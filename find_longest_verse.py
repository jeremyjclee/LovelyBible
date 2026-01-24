import json
import sys

def find_longest_verse(file_path):
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        longest_key = None
        longest_text = ""
        max_length = 0
        
        for key, text in data.items():
            # Check if key ends with :1 (verse 1)
            # keys are like "BookChapter:Verse"
            if key.endswith(":1"):
                # Double check splitting to be sure
                parts = key.split(":")
                if len(parts) == 2 and parts[1] == "1":
                    if len(text) > max_length:
                        max_length = len(text)
                        longest_text = text
                        longest_key = key
                        
        with open("result.txt", "w", encoding="utf-8") as out:
            out.write(f"Longest Key: {longest_key}\n")
            out.write(f"Length: {max_length}\n")
            out.write(f"Text: {longest_text}\n")
            
        print("Done. Check result.txt")
        
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    file_path = "C:\\Users\\jerem\\AndroidStudioProjects\\LovelyBible\\sample\\composeApp\\src\\jvmMain\\resources\\bible.json"
    find_longest_verse(file_path)
