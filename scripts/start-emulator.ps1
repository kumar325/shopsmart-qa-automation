# Starts the shopsmart_api34 Android emulator and makes sure its window is visible.
#
# Why this exists: on a high-DPI display (e.g. 4K at 200% scaling) the emulator can
# place its window outside the visible desktop, so it looks like it is "running in
# the background". After launch we move and resize the window to fit the screen.

param(
    [string]$Avd = "shopsmart_api34",
    [string]$Gpu = "host"
)

$sdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { "$env:LOCALAPPDATA\Android\Sdk" }
$emulator = Join-Path $sdk "emulator\emulator.exe"

Add-Type @'
using System; using System.Runtime.InteropServices;
public static class Win {
    [DllImport("user32.dll")] public static extern bool SetProcessDPIAware();
    [DllImport("user32.dll")] public static extern bool SetWindowPos(IntPtr h, IntPtr after, int x, int y, int cx, int cy, uint flags);
    [DllImport("user32.dll")] public static extern bool GetWindowRect(IntPtr h, out RECT r);
    [DllImport("user32.dll")] public static extern bool ShowWindow(IntPtr h, int cmd);
    [DllImport("user32.dll")] public static extern bool SetForegroundWindow(IntPtr h);
    public struct RECT { public int Left, Top, Right, Bottom; }
}
'@
Add-Type -AssemblyName System.Windows.Forms
# Work in physical pixels so window and screen coordinates use the same units.
[Win]::SetProcessDPIAware() | Out-Null

Start-Process $emulator -ArgumentList "-avd $Avd -gpu $Gpu" -WorkingDirectory (Split-Path $emulator)

# The device window belongs to qemu-system-x86_64 and appears a few seconds after launch.
$window = [IntPtr]::Zero
for ($i = 0; $i -lt 60 -and $window -eq [IntPtr]::Zero; $i++) {
    Start-Sleep -Seconds 1
    $qemu = Get-Process qemu-system-x86_64 -ErrorAction SilentlyContinue |
        Where-Object { $_.MainWindowTitle -like "Android Emulator*" } | Select-Object -First 1
    if ($qemu) { $window = $qemu.MainWindowHandle }
}
if ($window -eq [IntPtr]::Zero) { throw "Emulator window did not appear within 60 seconds." }
Start-Sleep -Seconds 2   # let the emulator finish its own initial placement first

# Fit the window into 90% of the screen height, keeping the phone's aspect ratio,
# and centre it on the primary screen.
$area = [System.Windows.Forms.Screen]::PrimaryScreen.WorkingArea
$rect = New-Object Win+RECT
[Win]::GetWindowRect($window, [ref]$rect) | Out-Null
$width = $rect.Right - $rect.Left
$height = $rect.Bottom - $rect.Top
$scale = [Math]::Min(1.0, ($area.Height * 0.9) / $height)
$newWidth = [int]($width * $scale)
$newHeight = [int]($height * $scale)
$x = $area.Left + [int](($area.Width - $newWidth) / 2)
$y = $area.Top + [int](($area.Height - $newHeight) / 2)

[Win]::ShowWindow($window, 9) | Out-Null          # 9 = SW_RESTORE
[Win]::SetWindowPos($window, [IntPtr]::Zero, $x, $y, $newWidth, $newHeight, 0x0040) | Out-Null  # SWP_SHOWWINDOW
[Win]::SetForegroundWindow($window) | Out-Null
