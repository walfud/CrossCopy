#!/bin/bash +vx

# env
installDir=/usr/local/bin/cc
version=$(curl -H "Accept: application/vnd.github+json" https://api.github.com/repos/walfud/HelloInterpolator/releases/latest | grep '"tag_name":' | head -1 | sed -e 's/^.*v//' | sed -e 's/".*$//')

echo $version

# download
tmpDir=/tmp/com.walfud.crosscopy
mkdir -p "$tmpDir"
curl -o "$tmpDir/cc.zip" -L https://walfud-shanghai.oss-cn-shanghai.aliyuncs.com/cc-1.0.0.zip
unzip -o -d "$tmpDir" "$tmpDir/cc.zip"

mkdir -p "$installDir"
cp -r "$tmpDir/cc-$version/bin" "$tmpDir/cc-$version/lib" "$installDir"

# link to path
chmod +x $installDir/bin/cc
rm -rf /usr/local/bin/ccx
ln -s $installDir/bin/cc /usr/local/bin/ccx