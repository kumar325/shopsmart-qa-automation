# Network Captures

Wireshark capture files (`.pcap` / `.pcapng`) taken while running the test suites.

Raw captures are git-ignored by default because they can contain tokens, cookies and other
personal traffic from your machine. Only commit a capture after reviewing it and filtering it
down to the test traffic (File → Export Specified Packets in Wireshark).

See [docs/network-analysis.md](../docs/network-analysis.md) for the display filters and
troubleshooting workflow used with these captures.
