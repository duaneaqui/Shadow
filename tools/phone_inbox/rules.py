"""Cheap, no-AI rules: hide OTPs, and decide which notifications are worth an alert."""
import re

# Anything that looks like a one-time code is redacted before it is stored or forwarded.
_OTP_WORDS = re.compile(
    r"\b(otp|one[- ]?time|verification|verify|passcode|security code|auth(?:entication)? code|"
    r"login code|code is|your code|mpin|pin)\b", re.I)
_CODE = re.compile(r"\b\d{4,8}\b")

_MEETING = re.compile(
    r"\b(meeting|meet|sched(?:ule)?|resched(?:ule)?|move[d]?|postpone[d]?|cancel(?:led|ed)?|"
    r"call|sync|standup|update[s]? meeting|bukas|mamaya|tonight|tomorrow|"
    r"mon(?:day)?|tue(?:s(?:day)?)?|wed(?:nesday)?|thu(?:rs(?:day)?)?|fri(?:day)?|sat(?:urday)?|sun(?:day)?)\b",
    re.I)
_TIME = re.compile(r"\b\d{1,2}(?::\d{2})?\s?(?:am|pm)\b", re.I)
_MONEY = re.compile(
    r"(₱|\bphp\b|\bp\s?\d|\bdue\b|\bbayad\b|\bbayaran\b|\bpayment\b|\bpaid\b|\binstal(?:l)?ment\b|"
    r"\bspaylater\b|\bsloan\b|\bloan\b|\bbill(?:ing)?\b|\bauto[- ]?debit\b|\bdeduct(?:ed|ion)?\b|"
    r"\bcharged\b|\bpurchase\b|\btransaction\b|\breceived\b|\bsent you\b|\bsubscription\b|\boverdue\b)",
    re.I)


def redact_otp(text: str) -> str:
    if text and _OTP_WORDS.search(text) and _CODE.search(text):
        return _CODE.sub("[code hidden]", text)
    return text or ""


def classify(app: str, title: str, text: str) -> tuple[str, bool]:
    """Return (category, flagged). Flagged events get a Telegram alert."""
    blob = f"{title or ''} {text or ''}"
    if _MEETING.search(blob) and (_TIME.search(blob) or re.search(r"\b(meeting|resched|move)", blob, re.I)):
        return "meeting", True
    if _MONEY.search(blob):
        # Money notifications are always tracked; alert on due/overdue/deducted ones.
        urgent = re.search(r"\b(due|overdue|deduct|auto[- ]?debit|charged|installment|spaylater|sloan)", blob, re.I)
        return "money", bool(urgent)
    return "other", False
