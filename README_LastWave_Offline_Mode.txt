LastWave Offline Mode patch

1. Put this script in the root of your LastWave-Native-offline-music checkout.
2. Run:
   bash apply_lastwave_offline_mode.sh
3. Review:
   git diff --check
   git diff --stat
4. Do not push until the diff/build is reviewed.

The patch adds:
- Offline Music mode in the floating navigation pill.
- Animated/bouncy Offline <-> Official switch.
- Full-screen enter/exit transition.
- Offline mode that replaces the online main UI with the local music library.
- OkHttp network blocking while Offline Mode is active.
- Online playback rejection while Offline Mode is active.
- Automatic re-scan when the selected local music folder changes.
- Copyright attribution for new code:
  Copyright (C) 2026 Anshuman X (maxxcodebug)

The script intentionally stops if an expected source marker is missing, so it
will not silently apply a partial patch to a changed upstream file.
