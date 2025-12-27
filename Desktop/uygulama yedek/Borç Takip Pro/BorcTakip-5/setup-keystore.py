#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
BorcTakip Release Signing Kurulumu
Interactive setup script - Kullanıcı girdisi alır
"""

import os
import subprocess
import base64
import sys
import getpass
import shutil
from pathlib import Path

class Colors:
    """ANSI renk kodları"""
    BLUE = '\033[94m'
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    CYAN = '\033[96m'
    END = '\033[0m'
    BOLD = '\033[1m'

def print_title(text):
    """Başlık yazdır"""
    print()
    print("=" * 80)
    print(f" {text}")
    print("=" * 80)
    print()

def print_success(text):
    """Başarı mesajı"""
    print(f"{Colors.GREEN}✅ {text}{Colors.END}")

def print_error(text):
    """Hata mesajı"""
    print(f"{Colors.RED}❌ {text}{Colors.END}")

def print_info(text):
    """Bilgi mesajı"""
    print(f"{Colors.YELLOW}ℹ️  {text}{Colors.END}")

def print_step(number, text):
    """Adım başlığı"""
    print(f"{Colors.CYAN}[{number}/5] {text}{Colors.END}")
    print()

def run_command(cmd, description):
    """Komut çalıştır ve sonucu kontrol et"""
    print(f"{Colors.CYAN}{description}...{Colors.END}")
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    if result.returncode != 0:
        print_error(f"{description} başarısız oldu!")
        if result.stderr:
            print(result.stderr)
        return False
    return True

def main():
    """Ana kurulum fonksiyonu"""
    
    # Windows kontrol
    if sys.platform != "win32":
        print_error("Bu script sadece Windows'ta çalışır!")
        sys.exit(1)
    
    print_title("BorcTakip Release Signing Kurulumu")
    print(f"Bu script ile keystore kurulumunu yapacaksınız.")
    print(f"Kendi şifrelerinizi güvenli şekilde gireceksiniz.")
    print()
    
    keystore_path = "release-key.keystore"
    keystore_exists = os.path.exists(keystore_path)
    
    # ADIM 1: Keystore Kontrol
    print_step(1, "Keystore Dosyası Kontrol Ediliyor")
    
    if keystore_exists:
        file_size = os.path.getsize(keystore_path) / 1024
        print_success(f"release-key.keystore bulundu (Boyut: {file_size:.1f} KB)")
    else:
        print_error("release-key.keystore bulunamadı!")
        print()
        print("Seçenekler:")
        print("  A) Yeni keystore oluştur")
        print("  B) Mevcut keystore'u kopyala")
        print()
        
        choice = input("Seçiminiz (A/B): ").strip().upper()
        
        if choice == "A":
            print()
            print(f"{Colors.CYAN}Yeni keystore oluşturuluyor...{Colors.END}")
            print()
            
            key_cn = input("Adınız (Common Name) [Burhan]: ").strip() or "Burhan"
            key_ou = input("Organizasyon Birimi [BorcTakip]: ").strip() or "BorcTakip"
            key_o = input("Organizasyon Adı [BorcTakip]: ").strip() or "BorcTakip"
            key_l = input("Şehir [Turkey]: ").strip() or "Turkey"
            key_st = input("Bölge [Turkey]: ").strip() or "Turkey"
            key_c = input("Ülke Kodu [TR]: ").strip() or "TR"
            
            print()
            keystore_pass = getpass.getpass("Keystore Şifresi (min 6 karakter): ")
            if not keystore_pass:
                print_error("Şifre boş olamaz!")
                sys.exit(1)
            
            key_alias = input("Key Alias [release-key]: ").strip() or "release-key"
            key_pass = getpass.getpass("Key Şifresi (boş bırakırsa keystore şifresi kullanılır): ")
            if not key_pass:
                key_pass = keystore_pass
            
            # Keystore oluştur
            print()
            print(f"{Colors.CYAN}Keystore oluşturuluyor (RSA 2048, 10000 gün geçerli)...{Colors.END}")
            
            dname = f"CN={key_cn}, OU={key_ou}, O={key_o}, L={key_l}, ST={key_st}, C={key_c}"
            cmd = (
                f'keytool -genkeypair '
                f'-alias {key_alias} '
                f'-keyalg RSA '
                f'-keysize 2048 '
                f'-keystore {keystore_path} '
                f'-validity 10000 '
                f'-keypass {key_pass} '
                f'-storepass {keystore_pass} '
                f'-dname "{dname}"'
            )
            
            if run_command(cmd, "Keystore oluşturma"):
                print_success("Keystore başarıyla oluşturuldu!")
            else:
                sys.exit(1)
                
        elif choice == "B":
            print()
            print("Lütfen mevcut keystore dosyasını proje root'una kopyalayın.")
            print("Dosya adı: release-key.keystore")
            print()
            input("İşlem tamamlandığında Enter'a basın")
            print()
            
            if not os.path.exists(keystore_path):
                print_error("Keystore dosyası hala bulunamadı!")
                sys.exit(1)
            print_success("Keystore dosyası bulundu!")
        else:
            print_error("Geçersiz seçim!")
            sys.exit(1)
    
    # ADIM 2: local.properties Oluştur
    print_step(2, "local.properties Dosyası Oluşturuluyor")
    
    print("Keystore bilgilerini giriniz:")
    print()
    
    key_alias = input("Key Alias [release-key]: ").strip() or "release-key"
    keystore_pass = getpass.getpass("Keystore Şifresi: ")
    if not keystore_pass:
        print_error("Şifre boş olamaz!")
        sys.exit(1)
    
    key_pass = getpass.getpass("Key Şifresi (boş bırakırsa keystore şifresi kullanılır): ")
    if not key_pass:
        key_pass = keystore_pass
    
    sdk_dir = f"C:\\Users\\{os.getenv('USERNAME')}\\AppData\\Local\\Android\\Sdk"
    
    local_properties_content = f"""## This file is automatically generated by Android Studio.
# Do not modify this file -- YOUR CHANGES WILL BE ERASED!
#
# This file should *NOT* be checked into Version Control Systems,
# as it contains information specific to your local configuration.
#
sdk.dir={sdk_dir}

# Gemini API Key
GEMINI_API_KEY=AIzaSyAUzi7qz-V1dwomDaVWMO9gNGF4fQng4oM

# Release Signing Configuration
BORC_TAKIP_STORE_FILE=release-key.keystore
BORC_TAKIP_STORE_PASSWORD={keystore_pass}
BORC_TAKIP_KEY_ALIAS={key_alias}
BORC_TAKIP_KEY_PASSWORD={key_pass}
"""
    
    with open("local.properties", "w", encoding="utf-8") as f:
        f.write(local_properties_content)
    
    print_success("local.properties güncellenmiştir")
    
    # ADIM 3: Build Test
    print_step(3, "Lokal Build Test Ediliyor")
    print(f"{Colors.YELLOW}Komut: gradlew :app:assembleDebug{Colors.END}")
    print()
    
    if not run_command("gradlew.bat :app:assembleDebug", "Build test"):
        print_error("Build başarısız oldu!")
        print("Hataları kontrol edin ve tekrar deneyin.")
        input("Enter'a basın")
        sys.exit(1)
    
    print_success("Build başarılı!")
    
    # ADIM 4: Base64 SIGNING_KEY
    print_step(4, "Base64 SIGNING_KEY Oluşturuluyor")
    print()
    print(f"{Colors.CYAN}Base64 string oluşturuluyor...{Colors.END}")
    
    try:
        with open(keystore_path, "rb") as f:
            keystore_bytes = f.read()
        base64_string = base64.b64encode(keystore_bytes).decode("utf-8")
        
        # Clipboard'a kopyala (Windows)
        import subprocess
        process = subprocess.Popen("clip", stdin=subprocess.PIPE, shell=True)
        process.communicate(base64_string.encode("utf-8"))
        
        print_success("Base64 SIGNING_KEY oluşturuldu!")
        print(f"{Colors.GREEN}✅ Clipboard'a kopyalandı!{Colors.END}")
        print()
        print(f"Base64 String (ilk 50 karakter):")
        print(f"{Colors.GRAY}{base64_string[:50]}{Colors.END}")
        print("...")
        print()
        print(f"  Toplam uzunluk: {len(base64_string)} karakter")
    except Exception as e:
        print_error(f"Base64 oluşturma başarısız: {e}")
    
    # ADIM 5: GitHub Talimatları
    print_step(5, "GitHub Setup Talimatları")
    
    print(f"{Colors.CYAN}GitHub'da şu 4 Secret'i eklemeli siniz:{Colors.END}")
    print()
    
    print(f"{Colors.YELLOW}1. BORC_TAKIP_STORE_PASSWORD{Colors.END}")
    print(f"{Colors.GRAY}   Value: *** (girildi){Colors.END}")
    print()
    
    print(f"{Colors.YELLOW}2. BORC_TAKIP_KEY_ALIAS{Colors.END}")
    print(f"{Colors.GRAY}   Value: {key_alias}{Colors.END}")
    print()
    
    print(f"{Colors.YELLOW}3. BORC_TAKIP_KEY_PASSWORD{Colors.END}")
    print(f"{Colors.GRAY}   Value: *** (girildi){Colors.END}")
    print()
    
    print(f"{Colors.YELLOW}4. SIGNING_KEY{Colors.END}")
    print(f"{Colors.GRAY}   Value: (Clipboard'dan yapıştır - otomatik kopyalandı){Colors.END}")
    print()
    
    # Final özet
    print_title("KURULUM TAMAMLANDI!")
    
    print_success("Keystore oluşturuldu: release-key.keystore")
    print_success("local.properties güncellenmiştir")
    print_success("Build test başarılı")
    print_success("GitHub Secrets talimatları gösterildi")
    
    print()
    print(f"{Colors.CYAN}SONRA YAPACAK:{Colors.END}")
    print("  1. GitHub'da 4 Secret ekleyin")
    print(f"     👉 https://github.com/burhan2855/borctakip/settings/secrets/actions")
    print()
    print("  2. İlk test commit'i yapın:")
    print("     git push origin develop")
    print()
    print("  3. GitHub Actions'ta çalışmaları izleyin:")
    print(f"     👉 https://github.com/burhan2855/borctakip/actions")
    print()
    
    input("Tamamlamak için Enter'a basın")

if __name__ == "__main__":
    main()
