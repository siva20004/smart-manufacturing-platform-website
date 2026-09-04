import os

base_dir = r"c:\Users\dell\Desktop\nihon project\src\main\java\com\sivamachineworks\platform\pdm"
os.makedirs(os.path.join(base_dir, "domain"), exist_ok=True)
os.makedirs(os.path.join(base_dir, "repository"), exist_ok=True)
os.makedirs(os.path.join(base_dir, "dto"), exist_ok=True)
os.makedirs(os.path.join(base_dir, "service"), exist_ok=True)
os.makedirs(os.path.join(base_dir, "controller"), exist_ok=True)

test_dir = r"c:\Users\dell\Desktop\nihon project\src\test\java\com\sivamachineworks\platform\pdm"
os.makedirs(test_dir, exist_ok=True)
