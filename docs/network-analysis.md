# Network Analysis with Wireshark

How to capture and read the traffic produced by this project's tests, which display filters to use,
and a step-by-step method for troubleshooting network problems.

Every number and output in sections 3 and 4 comes from a real capture taken on 2026-10-21 while the
Newman suite ran (TShark 4.6.9, Npcap, Windows 11, Wi-Fi). Nothing is illustrative.

---

## 1. Capture setup

### Capture only what you need

A **capture filter** (BPF syntax, applied while recording) decides what gets saved. A **display
filter** (Wireshark syntax, applied afterwards) decides what you see. They use different languages:

| | Capture filter | Display filter |
|---|---|---|
| When | While recording | After recording |
| Syntax | BPF: `host 1.2.3.4 and port 443` | Wireshark: `ip.addr == 1.2.3.4 && tcp.port == 443` |
| Can be changed later? | No, unmatched packets are gone | Yes |

Resolve the target first, then restrict the capture to DNS plus those addresses so none of your
other traffic is recorded:

```bash
nslookup restful-booker.herokuapp.com
# -> 99.83.176.110, 166.117.94.78, 13.248.162.64, 35.71.177.79

tshark -D          # list interfaces; pick the active one (Wi-Fi here)
tshark -i "<interface>" \
  -f "port 53 or host 99.83.176.110 or host 166.117.94.78 or host 13.248.162.64 or host 35.71.177.79" \
  -a duration:45 -w captures/api-run.pcapng
```

`ipconfig /flushdns` before the run forces a fresh DNS lookup, so it appears in the capture.

### Decrypting your own HTTPS traffic

TLS hides HTTP from anyone on the network, including you. To debug your own client, have it write
its session keys to a file, then give that file to Wireshark:

```bash
# Node.js (Newman) - built-in flag
NODE_OPTIONS="--tls-keylog=captures/tls.keylog" newman run postman/ShopSmart-API.postman_collection.json ...

# Chrome / Edge honour the SSLKEYLOGFILE environment variable
set SSLKEYLOGFILE=C:\path\to\tls.keylog

# Read with keys
tshark -r captures/api-run.pcapng -o "tls.keylog_file:captures/tls.keylog" -Y http
```

In the Wireshark GUI: *Edit → Preferences → Protocols → TLS → (Pre)-Master-Secret log filename*.

> The key log file can decrypt every session it covers. Treat it like a password: it is git-ignored
> and was deleted immediately after this analysis.

---

## 2. Display filter reference

| Goal | Display filter |
|---|---|
| All traffic to/from one host | `ip.addr == 35.71.177.79` |
| DNS lookups for a name | `dns.qry.name contains "restful-booker"` |
| DNS failures (NXDOMAIN, SERVFAIL...) | `dns.flags.rcode != 0` |
| Slow DNS answers (>200 ms) | `dns.time > 0.2` |
| New TCP connections (SYN only) | `tcp.flags.syn == 1 && tcp.flags.ack == 0` |
| Connection refused (RST) | `tcp.flags.reset == 1` |
| Any TCP problem Wireshark detected | `tcp.analysis.flags` |
| Retransmissions only | `tcp.analysis.retransmission` |
| Zero window (receiver overwhelmed) | `tcp.analysis.zero_window` |
| TLS ClientHello (shows SNI hostname) | `tls.handshake.type == 1` |
| TLS for one hostname | `tls.handshake.extensions_server_name == "restful-booker.herokuapp.com"` |
| TLS alerts (handshake failures, cert problems) | `tls.alert_message` |
| Plain HTTP on port 80 | `http && tcp.port == 80` |
| HTTP requests of one method | `http.request.method == "POST"` |
| Client and server errors | `http.response.code >= 400` |
| Server errors only | `http.response.code >= 500` |
| Slow responses (>1 s server time) | `http.time > 1` |
| Requests carrying a cookie | `http.cookie` |
| Requests with an Authorization header | `http.authorization` |
| Follow one connection | `tcp.stream == 0` |

All filters above were syntax-checked with `tshark -Y` against the capture.

---

## 3. Walkthrough of the captured Newman run

The capture holds **144 packets**: 44 DNS, 100 TCP, of which 55 are TLS and 2 are plain HTTP.

### 3.1 DNS: `dns.qry.name contains "restful-booker"`

| Frame | Query type | Answer | Response time (`dns.time`) |
|---|---|---|---|
| 1 / 3 | A (1) | 35.71.177.79, 99.83.176.110, 13.248.162.64, 166.117.94.78 | 38 ms |
| 2 / 4 | AAAA (28) | *(empty)* | 38 ms |

The client asks for IPv4 and IPv6 in parallel. The empty AAAA answer means the host has no IPv6
address, which is normal, not an error. Four A records means the service is load-balanced.

### 3.2 TCP: `tcp.flags.syn == 1 && tcp.flags.ack == 0`

Only **two** connections were opened for 27 requests:

| Stream | Server | Frames | Purpose |
|---|---|---|---|
| 0 | 35.71.177.79:443 | 91 | All 26 Newman requests over HTTPS |
| 1 | 35.71.177.79:80 | 9 | One manual plain-HTTP request (3.5) |

Newman reuses one connection (HTTP keep-alive) instead of paying a new TCP + TLS handshake per
request. The SYN → SYN/ACK round-trip time (`tcp.analysis.ack_rtt` on frame 6) was **80 ms**, which
is the network latency floor for every request.

`tcp.analysis.flags` returned **no packets**: no retransmissions, duplicate ACKs or zero windows,
so the network path was healthy.

### 3.3 TLS: `tls.handshake.type == 1` / `== 2`

| Field | Value | Meaning |
|---|---|---|
| SNI (frame 8) | `restful-booker.herokuapp.com` | Hostname is visible to observers even with HTTPS |
| Negotiated version (frame 11) | `0x0304` | TLS 1.3 |
| Cipher suite | `0x1301` | TLS_AES_128_GCM_SHA256 |
| ClientHello → ServerHello | 0.127 s → 0.210 s | TLS 1.3 completes in **one** round trip |

Without the key log, `http && tcp.port == 443` returns **0 packets**. An observer sees only that
the client talked to this host, how much and when, but not what was said.

### 3.4 Decrypted HTTP: `http` with the key log loaded

Every request and its response become visible. Filtering on `http.response.code >= 400` returned
exactly the negative tests in the collection, which proves the traffic matches the test intent:

| Frame | Status | URI | Collection test |
|---|---|---|---|
| 46 | 404 Not Found | /booking/188 | Get deleted booking |
| 52, 58, 61 | 403 Forbidden | /booking/198 | PUT/PATCH/DELETE without valid auth |
| 64 | 404 Not Found | /booking/999999999 | Non-existent id |
| 67 | 404 Not Found | /booking/abc | Non-numeric id |
| 70 | 500 Internal Server Error | /booking | Missing fields (BUG-API-02) |
| 73 | 400 Bad Request | /booking | Malformed JSON |
| 82 | 405 Method Not Allowed | /booking/188 | Delete twice (BUG-API-06) |

**Server time** (`http.time`, request sent → response received) ranged from **71 ms to 85 ms**.
That is almost exactly the 80 ms network RTT, so the server itself does very little work per
request. If a test were slow, this split tells you whether to blame the network or the backend.

Decryption also exposes secrets: the `token=...` cookie (frames 36, 42) and the PATCH request's
`Authorization: Basic YWRtaW46cGFzc3dvcmQxMjM=`, which Wireshark decodes as `admin:password123`
(frame 39). Basic auth is only Base64-encoded, not encrypted; TLS is what protects it.

### 3.5 Finding: credentials sent over plain HTTP

Restful Booker answers `http://` requests directly instead of redirecting to HTTPS. One
`POST /auth` over port 80 was captured with `http && tcp.port == 80`, **without any keys**:

```
frame 103  POST /auth   body: {"username":"admin","password":"password123"}
frame 105  200          body: {"token":"2acfbd73bef70df"}
```

Anyone on the same network could read the credentials and the issued token. This is logged as
**BUG-API-07** in [bug-reports.md](bug-reports.md). The tests always use `https://`.

---

## 4. Troubleshooting method

Work up the stack: each layer depends on the one below it, so the first layer that fails is your
root cause.

| Step | Question | Filter | Healthy | Unhealthy → likely cause |
|---|---|---|---|---|
| 1 | Did the name resolve? | `dns.qry.name contains "<host>"` | Response with A records | No response → DNS server unreachable; `dns.flags.rcode != 0` → wrong hostname / NXDOMAIN |
| 2 | Did TCP connect? | `tcp.flags.syn == 1` | SYN → SYN/ACK → ACK | SYN repeated with no reply → firewall/proxy drop; `tcp.flags.reset == 1` right after SYN → port closed |
| 3 | Did TLS succeed? | `tls.handshake.type` | ClientHello → ServerHello | `tls.alert_message` → cert or version mismatch; a corporate proxy may present its own certificate |
| 4 | Is the path healthy? | `tcp.analysis.flags` | Nothing | Retransmissions → packet loss; zero window → slow receiver |
| 5 | What did the app return? | `http.response.code >= 400` (keys needed for HTTPS) | Expected codes | 401/403 → auth header/cookie missing; 5xx → server bug; compare request body with the API docs |
| 6 | Where is time spent? | `dns.time`, `tcp.analysis.ack_rtt`, `http.time` | Small, consistent | High `dns.time` → DNS; high RTT → network; `http.time` far above RTT → slow backend |

### Typical test-failure scenarios

| Symptom in test report | What to look for in the capture |
|---|---|
| `ENOTFOUND` / `UnknownHostException` | Step 1. Is there a DNS query? Was the answer empty or an error? |
| `ECONNREFUSED` | Step 2. RST in reply to SYN: nothing is listening on that port. |
| `ETIMEDOUT` / connect timeout | Step 2. SYNs retransmitted with no answer: something is dropping traffic. |
| `CERT_*` / SSL handshake error | Step 3. Look for a TLS alert, and check whether the certificate comes from a proxy. |
| Test passes locally but is flaky in CI | Steps 4 and 6. Retransmissions or `http.time` outliers in the CI capture. |
| Assertion fails on status code | Step 5. Decrypt and compare the exact request sent with what the test intended. |

---

## 5. Interview talking points

- **Capture vs display filter**: BPF at capture time cannot be undone; display filters can.
- **What HTTPS hides and what it doesn't**: payload, URLs and headers are hidden. IPs, ports,
  timing, sizes, DNS queries and the SNI hostname remain visible.
- **TLS 1.3 vs 1.2**: 1 round trip vs 2, and the certificate is encrypted in 1.3.
- **Keep-alive**: one TCP/TLS handshake reused for 26 requests; without it, each request would pay
  roughly 2 × RTT extra.
- **Separating network latency from server latency**: compare `tcp.analysis.ack_rtt` with `http.time`.
- **Basic auth is encoding, not encryption**: Base64 is reversible by anyone.
