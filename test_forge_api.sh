#!/bin/bash

# Forge API 测试脚本
# 用于测试官方源和 BMCLAPI 的 Forge 版本列表获取

echo "=========================================="
echo "Forge API 测试脚本"
echo "=========================================="
echo ""

# 测试的 Minecraft 版本
MC_VERSION="1.14"

echo "测试版本: $MC_VERSION"
echo ""

# 1. 测试官方源 - HTML 方式
echo "1. 测试官方源 (HTML 方式)"
echo "URL: https://files.minecraftforge.net/maven/net/minecraftforge/forge/index_${MC_VERSION}.html"
OFFICIAL_HTML_URL="https://files.minecraftforge.net/maven/net/minecraftforge/forge/index_${MC_VERSION}.html"
HTTP_CODE=$(curl -s -o /tmp/forge_official.html -w "%{http_code}" "$OFFICIAL_HTML_URL")
echo "HTTP 状态码: $HTTP_CODE"

if [ "$HTTP_CODE" = "200" ]; then
    FILE_SIZE=$(wc -c < /tmp/forge_official.html)
    echo "文件大小: $FILE_SIZE 字节"
    if [ "$FILE_SIZE" -lt 100 ]; then
        echo "⚠️  警告: 响应内容过短"
    else
        echo "✅ 成功获取 HTML"
        # 显示前 500 字符
        echo "内容预览:"
        head -c 500 /tmp/forge_official.html
        echo ""
    fi
else
    echo "❌ 获取失败"
fi
echo ""

# 2. 测试官方源 - BMCLAPI 方式
echo "2. 测试 BMCLAPI (JSON 方式)"
echo "URL: https://bmclapi2.bangbang93.com/forge/minecraft/${MC_VERSION}"
BMCLAPI_URL="https://bmclapi2.bangbang93.com/forge/minecraft/${MC_VERSION}"
HTTP_CODE=$(curl -s -o /tmp/forge_bmclapi.json -w "%{http_code}" "$BMCLAPI_URL")
echo "HTTP 状态码: $HTTP_CODE"

if [ "$HTTP_CODE" = "200" ]; then
    FILE_SIZE=$(wc -c < /tmp/forge_bmclapi.json)
    echo "文件大小: $FILE_SIZE 字节"
    if [ "$FILE_SIZE" -lt 10 ]; then
        echo "⚠️  警告: 响应内容过短"
    else
        echo "✅ 成功获取 JSON"
        echo "内容预览:"
        cat /tmp/forge_bmclapi.json
        echo ""
    fi
else
    echo "❌ 获取失败"
fi
echo ""

# 3. 测试其他可能的 URL 格式
echo "3. 测试其他可能的 URL 格式"

# 测试带下划线的版本
MC_VERSION_UNDERSCORE="${MC_VERSION//-/_}"
echo "测试带下划线的版本: $MC_VERSION_UNDERSCORE"
OFFICIAL_HTML_URL_UNDERSCORE="https://files.minecraftforge.net/maven/net/minecraftforge/forge/index_${MC_VERSION_UNDERSCORE}.html"
HTTP_CODE=$(curl -s -o /tmp/forge_official_underscore.html -w "%{http_code}" "$OFFICIAL_HTML_URL_UNDERSCORE")
echo "URL: $OFFICIAL_HTML_URL_UNDERSCORE"
echo "HTTP 状态码: $HTTP_CODE"
if [ "$HTTP_CODE" = "200" ]; then
    FILE_SIZE=$(wc -c < /tmp/forge_official_underscore.html)
    echo "文件大小: $FILE_SIZE 字节"
    if [ "$FILE_SIZE" -ge 100 ]; then
        echo "✅ 成功获取"
    fi
fi
echo ""

# 测试 BMCLAPI 带下划线的版本
BMCLAPI_URL_UNDERSCORE="https://bmclapi2.bangbang93.com/forge/minecraft/${MC_VERSION_UNDERSCORE}"
HTTP_CODE=$(curl -s -o /tmp/forge_bmclapi_underscore.json -w "%{http_code}" "$BMCLAPI_URL_UNDERSCORE")
echo "URL: $BMCLAPI_URL_UNDERSCORE"
echo "HTTP 状态码: $HTTP_CODE"
if [ "$HTTP_CODE" = "200" ]; then
    FILE_SIZE=$(wc -c < /tmp/forge_bmclapi_underscore.json)
    echo "文件大小: $FILE_SIZE 字节"
    if [ "$FILE_SIZE" -ge 10 ]; then
        echo "✅ 成功获取"
    fi
fi
echo ""

# 4. 测试 Forge Maven 目录列表
echo "4. 测试 Forge Maven 目录列表"
MAVEN_DIR_URL="https://files.minecraftforge.net/maven/net/minecraftforge/forge/"
HTTP_CODE=$(curl -s -o /tmp/forge_maven_dir.html -w "%{http_code}" "$MAVEN_DIR_URL")
echo "URL: $MAVEN_DIR_URL"
echo "HTTP 状态码: $HTTP_CODE"
if [ "$HTTP_CODE" = "200" ]; then
    FILE_SIZE=$(wc -c < /tmp/forge_maven_dir.html)
    echo "文件大小: $FILE_SIZE 字节"
    if [ "$FILE_SIZE" -ge 100 ]; then
        echo "✅ 成功获取"
        # 查找包含 1.14 的目录
        echo "查找 $MC_VERSION 相关目录:"
        grep -o "href=\"[^\"]*${MC_VERSION}[^\"]*\"" /tmp/forge_maven_dir.html | head -10
    fi
fi
echo ""

# 5. 测试 BMCLAPI 版本列表
echo "5. 测试 BMCLAPI 版本列表"
BMCLAPI_VERSIONS_URL="https://bmclapi2.bangbang93.com/forge/minecraft"
HTTP_CODE=$(curl -s -o /tmp/bmclapi_versions.json -w "%{http_code}" "$BMCLAPI_VERSIONS_URL")
echo "URL: $BMCLAPI_VERSIONS_URL"
echo "HTTP 状态码: $HTTP_CODE"
if [ "$HTTP_CODE" = "200" ]; then
    FILE_SIZE=$(wc -c < /tmp/bmclapi_versions.json)
    echo "文件大小: $FILE_SIZE 字节"
    if [ "$FILE_SIZE" -ge 10 ]; then
        echo "✅ 成功获取版本列表"
        echo "查找 $MC_VERSION 相关版本:"
        grep -o "\"[^\"]*${MC_VERSION}[^\"]*\"" /tmp/bmclapi_versions.json | head -5
    fi
fi
echo ""

# 6. 清理临时文件
echo "清理临时文件..."
rm -f /tmp/forge_official.html /tmp/forge_bmclapi.json /tmp/forge_official_underscore.html /tmp/forge_bmclapi_underscore.json /tmp/forge_maven_dir.html /tmp/bmclapi_versions.json

echo "=========================================="
echo "测试完成"
echo "=========================================="