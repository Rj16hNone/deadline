# -*- coding: utf-8 -*-
"""版本号与更新检查。

每次启动问一次 Gitee 的最新发行版，和本机版本比一比；网络不通、接口变了、
限流了，一律当作「没有更新」——不弹窗、不报错、不写日志，不打扰使用。
"""
import json
import re
import urllib.request

# 本机版本，唯一来源；发版时只改这里
VERSION = "1.1.0"

OWNER = "rj16h_none"
REPO = "deadline"
API = "https://gitee.com/api/v5/repos/%s/%s/releases/latest" % (OWNER, REPO)
PAGE = "https://gitee.com/%s/%s/releases" % (OWNER, REPO)


def _nums(text):
    """把 V1.2.3 / 1.2 这类版本号拆成可比较的整数元组。"""
    parts = re.findall(r"\d+", str(text or ""))
    return tuple(int(x) for x in parts[:3]) if parts else ()


def _newer(a, b):
    n = max(len(a), len(b))
    a = a + (0,) * (n - len(a))
    b = b + (0,) * (n - len(b))
    return a > b


def check(timeout=6):
    """有新版本返回 {'version', 'url'}，否则（含出错）返回 None。"""
    try:
        req = urllib.request.Request(
            API, headers={"User-Agent": "Deadline/%s" % VERSION})
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            data = json.loads(resp.read().decode("utf-8", "replace"))
    except Exception:
        return None

    tag = str(data.get("tag_name") or "").strip()
    latest = _nums(tag)
    if not latest or not _newer(latest, _nums(VERSION)):
        return None
    return {"version": tag.lstrip("Vv") or tag, "url": PAGE}
