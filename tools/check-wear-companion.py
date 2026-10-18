#!/usr/bin/env python3
"""Check built phone/watch identity contracts with Android SDK tools; no device pairing claim.

Run after assembling both identity flavors in :app and :wear. Debug requires matching
certificates. --allow-unsigned permits release metadata checks without private keys.
"""
import argparse
import os
from pathlib import Path
import re
import subprocess
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
ANDROID = "{http://schemas.android.com/apk/res/android}"


def run(*args):
    result = subprocess.run([str(x) for x in args], capture_output=True, text=True, check=True)
    return result.stdout


def properties(path):
    return dict(line.strip().split("=", 1) for line in path.read_text().splitlines()
                if line.strip() and not line.lstrip().startswith("#"))


def element(parent, tag, name):
    return next(x for x in parent.findall(tag) if x.get(ANDROID + "name") == name)


def check_listener(app, name, actions):
    service = element(app, "service", name)
    assert service.get(ANDROID + "exported") == "true", name
    assert actions <= {x.get(ANDROID + "name") for x in service.findall("intent-filter/action")}, name
    assert any(x.get(ANDROID + "pathPrefix") == "/dictate"
               and x.get(ANDROID + "scheme") == "wear"
               for x in service.findall("intent-filter/data")), name


def check_apk(module, flavor, args, sdk, upstream, custom):
    output = ROOT / module / "build/outputs/apk" / flavor / args.build_type
    apks = list(output.glob("*.apk"))
    assert len(apks) == 1, f"Expected exactly one APK in {output}, found {len(apks)}"
    apk = apks[0]
    aapt = sdk / "build-tools" / args.build_tools / "aapt2"
    analyzer = sdk / "cmdline-tools/latest/bin/apkanalyzer"
    badging = run(aapt, "dump", "badging", apk)
    resources = run(aapt, "dump", "resources", apk)
    manifest = ET.fromstring(run(analyzer, "manifest", "print", apk))
    app = manifest.find("application")
    custom_flavor = flavor == "custom"
    package = "net.devemperor.dictate" + (".jack" if custom_flavor else "")
    if args.build_type == "debug":
        package += ".debug"
    assert manifest.get("package") == package, apk

    # Expected contracts are independent of the Gradle flavor implementation.
    if custom_flavor:
        code = custom.get("wearVersionCode", custom["versionCode"]) if module == "wear" else custom["versionCode"]
        version = custom.get("wearVersionName", custom["versionName"]) if module == "wear" else custom["versionName"]
    else:
        code = str(int(upstream["projectVersionCode"]) + (100_004 if module == "wear" else 0))
        version = upstream["projectVersionName"].split("-", 1)[0]
        if module == "wear":
            version += "-w4"  # Original Wear revision contract, intentionally unchanged.
        elif args.build_type == "release" and "-" in upstream["projectVersionName"]:
            version += "-" + upstream["projectVersionName"].split("-", 1)[1]
    assert manifest.get(ANDROID + "versionCode") == code, apk
    actual_version = manifest.get(ANDROID + "versionName")
    if module == "app" and args.build_type == "debug":
        assert re.fullmatch(re.escape(version) + r"-debug\+[0-9a-f]+", actual_version), apk
    else:
        assert actual_version == version, apk

    label = "Dictate Custom" if custom_flavor else ("Dictate Debug" if module == "app" and args.build_type == "debug" else "Dictate")
    assert f"application-label:'{label}'" in badging, apk
    if custom_flavor:
        assert set(re.findall(r"application-label(?:-[^:]+)?:'([^']*)'", badging)) == {label}, apk

    def resolve_label(ref):
        if not ref.startswith("@"):
            return ref
        key = re.escape(ref.replace("@ref/", "").lstrip("@"))
        pattern = (r"resource " + key + r" string/[^\n]+\n\s+\(\) ([^\n]+)"
                   if ref.startswith(("@ref/", "@0x")) else r"resource 0x[0-9a-f]+ " + key + r"\n\s+\(\) ([^\n]+)")
        value = re.search(pattern, resources).group(1)
        return resolve_label(value) if value.startswith("@") else value.strip('"')

    permissions = {x.get(ANDROID + "name") for x in manifest.findall("uses-permission")}
    assert {"android.permission.RECORD_AUDIO", "android.permission.INTERNET"} <= permissions, apk
    providers = {x.get(ANDROID + "authorities") for x in app.findall("provider")}
    declared_permissions = {x.get(ANDROID + "name") for x in manifest.findall("permission")}
    assert all(x.startswith(package + ".") for x in providers | declared_permissions), apk
    assert manifest.get(ANDROID + "sharedUserId") is None, apk

    if module == "wear":
        assert element(manifest, "uses-feature", "android.hardware.type.watch").get(ANDROID + "required", "true") == "true"
        assert app.get(ANDROID + "allowBackup") == "false"
        assert element(app, "meta-data", "com.google.android.wearable.standalone").get(ANDROID + "value") == "true"
        assert manifest.find("uses-sdk").get(ANDROID + "minSdkVersion") == "30"
        ime = element(app, "service", "net.devemperor.dictate.wear.ime.WearImeService")
        assert ime.get(ANDROID + "permission") == "android.permission.BIND_INPUT_METHOD"
        assert ime.get(ANDROID + "exported") == "true"
        assert resolve_label(ime.get(ANDROID + "label")) == ("Dictate Custom" if custom_flavor else "Dictate (Wear)")
        assert any(x.get(ANDROID + "name") == "android.view.InputMethod" for x in ime.findall("intent-filter/action"))
        element(ime, "meta-data", "android.view.im")
        method_path = re.search(r"xml/method\n\s+\(\) \(file\) (\S+)", resources).group(1)
        method = ET.fromstring(run(analyzer, "resources", "xml", "--file", method_path, apk))
        settings_class = method.get(ANDROID + "settingsActivity")
        assert settings_class == "net.devemperor.dictate.wear.ui.WearSettingsActivity"
        element(app, "activity", settings_class)
        check_listener(app, "net.devemperor.dictate.wear.sync.WearSyncListenerService",
                       {"com.google.android.gms.wearable.DATA_CHANGED"})
    else:
        check_listener(app, "dev.patrickgold.florisboard.dictate.wear.DictateWearService",
                       {"com.google.android.gms.wearable.MESSAGE_RECEIVED", "com.google.android.gms.wearable.CHANNEL_EVENT"})
        # GMS looks this array up dynamically. Original release currently shrinks it;
        # its existing connected-node fallback is preserved. Custom must advertise it.
        if custom_flavor or args.build_type == "debug":
            assert re.search(r"array/android_wear_capabilities\n\s+\(\) \(array\) size=1\n\s+\[\"dictate_phone_transcriber\"\]", resources), apk

    merged_path = ROOT / module / "build/intermediates/merged_manifests" / (flavor + args.build_type.title())
    merged = list(merged_path.glob("*/AndroidManifest.xml"))
    assert len(merged) == 1, merged_path
    merged_manifest = ET.parse(merged[0]).getroot()
    for attr in ("package", ANDROID + "versionCode", ANDROID + "versionName"):
        assert merged_manifest.get(attr) == manifest.get(attr), (merged_path, attr)

    cert_result = subprocess.run([str(sdk / "build-tools" / args.build_tools / "apksigner"),
                                  "verify", "--print-certs", str(apk)], capture_output=True, text=True)
    if cert_result.returncode == 0:
        cert = re.search(r"certificate SHA-256 digest: ([0-9a-f]+)", cert_result.stdout).group(1)
    else:
        assert args.build_type == "release" and args.allow_unsigned, f"Unsigned/unverified APK: {apk}"
        assert "Missing META-INF/MANIFEST.MF" in cert_result.stderr + cert_result.stdout, apk
        cert = None
    print(f"PASS {module}/{flavor}/{args.build_type}: {package}, {actual_version} ({code}), {label}; "
          + (f"certificate {cert}" if cert else "unsigned; production pairing not validated"))
    return package, providers, declared_permissions, cert


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--sdk", type=Path, default=os.environ.get("ANDROID_HOME"))
    parser.add_argument("--build-tools", default="36.0.0")
    parser.add_argument("--build-type", choices=("debug", "release"), required=True)
    parser.add_argument("--allow-unsigned", action="store_true")
    args = parser.parse_args()
    assert args.sdk is not None, "Supply --sdk or ANDROID_HOME"
    upstream = properties(ROOT / "gradle.properties")
    custom = properties(ROOT / "custom-version.properties")
    results = {(module, flavor): check_apk(module, flavor, args, args.sdk, upstream, custom)
               for module in ("app", "wear") for flavor in ("original", "custom")}
    for flavor in ("original", "custom"):
        phone, watch = results["app", flavor], results["wear", flavor]
        assert phone[0] == watch[0], f"Mismatched Data Layer package for {flavor}"
        assert phone[3] == watch[3], f"Mismatched signing certificate for {flavor}"
    for module in ("app", "wear"):
        original, custom_app = results[module, "original"], results[module, "custom"]
        assert original[0] != custom_app[0]
        assert original[1].isdisjoint(custom_app[1]), f"Provider conflict in {module}"
        assert original[2].isdisjoint(custom_app[2]), f"Permission conflict in {module}"
    print("PASS phone/watch package identity contracts and side-by-side authority/permission checks")
    print("Device pairing, sync, recording and delivery still require paired-device testing.")


if __name__ == "__main__":
    main()
