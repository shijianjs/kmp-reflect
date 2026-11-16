# Inlined resource

This file lives in `demo/data/`. The KSP processor read it at compile time and wrote its bytes into
generated Kotlin as a Base64 literal, so the demo page can show it without any network request.
