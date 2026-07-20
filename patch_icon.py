import ctypes, struct, os, sys

exe = sys.argv[1]
ico = sys.argv[2]

os.chmod(exe, 0o644)

k32 = ctypes.WinDLL('kernel32', use_last_error=True)
k32.BeginUpdateResourceW.argtypes = [ctypes.c_wchar_p, ctypes.c_bool]
k32.BeginUpdateResourceW.restype = ctypes.c_void_p
k32.UpdateResourceW.argtypes = [ctypes.c_void_p, ctypes.c_void_p, ctypes.c_void_p, ctypes.c_uint16, ctypes.c_void_p, ctypes.c_ulong]
k32.UpdateResourceW.restype = ctypes.c_bool
k32.EndUpdateResourceW.argtypes = [ctypes.c_void_p, ctypes.c_bool]
k32.EndUpdateResourceW.restype = ctypes.c_bool

def res_id(i):
    """MAKEINTRESOURCE: pack a WORD as a pointer"""
    return ctypes.c_void_p(i & 0xFFFF)

RT_ICON = 3
RT_GROUP_ICON = 14

with open(ico, 'rb') as f:
    ico_data = f.read()

_, img_type, count = struct.unpack_from('<HHH', ico_data, 0)
assert img_type == 1

entries = []
for i in range(count):
    off = 6 + i * 16
    w, h, colors, reserved, planes, bpp, size, offset = struct.unpack_from('<BBBBHHII', ico_data, off)
    entries.append((w, h, colors, reserved, planes, bpp, size, offset))

handle = k32.BeginUpdateResourceW(exe, False)
if not handle:
    raise RuntimeError(f'BeginUpdateResourceW failed: error {ctypes.get_last_error()}')

group_data = struct.pack('<HHH', 0, 1, count)
for i, (w, h, colors, reserved, planes, bpp, size, _offset) in enumerate(entries):
    group_data += struct.pack('<BBBBHHIH', w, h, colors, reserved, planes, bpp, size, i + 1)

print(f'Group icon ({len(group_data)} bytes)...', end=' ')
ok = k32.UpdateResourceW(handle, res_id(RT_GROUP_ICON), res_id(32579), 0x0409, group_data, len(group_data))
print('OK' if ok else 'FAIL')
if not ok:
    print(f'  Error: {ctypes.get_last_error()}')

for i, (w, h, colors, reserved, planes, bpp, size, _offset) in enumerate(entries):
    icon_data = ico_data[_offset:_offset + size]
    print(f'Icon {i+1} ({w}x{h}, {len(icon_data)} bytes)...', end=' ')
    ok = k32.UpdateResourceW(handle, res_id(RT_ICON), res_id(i + 1), 0x0409, icon_data, len(icon_data))
    print('OK' if ok else 'FAIL')
    if not ok:
        print(f'  Error: {ctypes.get_last_error()}')

print('Committing...', end=' ')
ok = k32.EndUpdateResourceW(handle, False)
print('OK' if ok else 'FAIL')
if not ok:
    print(f'  Error: {ctypes.get_last_error()}')
