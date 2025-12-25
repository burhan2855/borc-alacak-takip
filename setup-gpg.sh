#!/bin/bash

# GPG Anahtar Kurulum Asistanı
# Bu script, GPG anahtar oluşturma ve Git yapılandırma sürecini kolaylaştırır

set -e

echo "============================================"
echo "🔐 GPG Anahtar Kurulum Asistanı"
echo "============================================"
echo ""

# Renk kodları
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# GPG kontrolü
if ! command -v gpg &> /dev/null; then
    echo -e "${RED}❌ GPG bulunamadı!${NC}"
    echo "Lütfen GPG'yi yükleyin:"
    echo "  Ubuntu/Debian: sudo apt-get install gnupg"
    echo "  Fedora: sudo dnf install gnupg"
    echo "  macOS: brew install gnupg"
    exit 1
fi

echo -e "${GREEN}✅ GPG bulundu: $(gpg --version | head -1)${NC}"
echo ""

# Mevcut anahtarları kontrol et
echo "Mevcut GPG anahtarlarınız kontrol ediliyor..."
if gpg --list-secret-keys --keyid-format=long 2>/dev/null | grep -q "sec"; then
    echo -e "${YELLOW}⚠️  Zaten GPG anahtarlarınız var:${NC}"
    gpg --list-secret-keys --keyid-format=long
    echo ""
    read -p "Yeni bir anahtar oluşturmak istiyor musunuz? (e/h): " CREATE_NEW
    if [[ ! "$CREATE_NEW" =~ ^[eE]$ ]]; then
        echo "Mevcut bir anahtarı kullanmak için anahtar ID'sini girin:"
        read -p "GPG Anahtar ID: " KEY_ID
        
        if [ -z "$KEY_ID" ]; then
            echo -e "${RED}❌ Anahtar ID boş olamaz!${NC}"
            exit 1
        fi
        
        # Git yapılandırması
        echo ""
        echo "Git yapılandırması yapılıyor..."
        git config --global user.signingkey "$KEY_ID"
        git config --global commit.gpgsign true
        git config --global tag.gpgsign true
        
        echo -e "${GREEN}✅ Git başarıyla yapılandırıldı!${NC}"
        echo ""
        echo "Public key'inizi GitHub'a eklemek için:"
        echo -e "${BLUE}gpg --armor --export $KEY_ID${NC}"
        exit 0
    fi
fi

echo ""
echo -e "${BLUE}Yeni GPG anahtar çifti oluşturuluyor...${NC}"
echo ""

# Kullanıcı bilgilerini al
read -p "👤 Adınız ve soyadınız: " FULL_NAME
read -p "📧 E-posta adresiniz (GitHub'da kullandığınız): " EMAIL
read -p "💬 Yorum (opsiyonel, örn: GitHub Signing Key): " COMMENT

# Boş alan kontrolü
if [ -z "$FULL_NAME" ] || [ -z "$EMAIL" ]; then
    echo -e "${RED}❌ Ad ve e-posta zorunludur!${NC}"
    exit 1
fi

# E-posta formatı kontrolü
if [[ ! "$EMAIL" =~ ^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$ ]]; then
    echo -e "${RED}❌ Geçersiz e-posta formatı!${NC}"
    exit 1
fi

echo ""
echo "Anahtar yapılandırması:"
echo "  - Tür: RSA 4096 bit"
echo "  - Geçerlilik: Sınırsız"
echo "  - Ad: $FULL_NAME"
echo "  - E-posta: $EMAIL"
if [ -n "$COMMENT" ]; then
    echo "  - Yorum: $COMMENT"
fi
echo ""

read -p "Devam etmek istiyor musunuz? (e/h): " CONFIRM
if [[ ! "$CONFIRM" =~ ^[eE]$ ]]; then
    echo "İşlem iptal edildi."
    exit 0
fi

# GPG anahtar oluşturma batch modu için geçici dosya
BATCH_FILE=$(mktemp)

cat > "$BATCH_FILE" <<EOF
Key-Type: RSA
Key-Length: 4096
Subkey-Type: RSA
Subkey-Length: 4096
Name-Real: $FULL_NAME
Name-Email: $EMAIL
EOF

if [ -n "$COMMENT" ]; then
    echo "Name-Comment: $COMMENT" >> "$BATCH_FILE"
fi

cat >> "$BATCH_FILE" <<EOF
Expire-Date: 0
%no-protection
%commit
EOF

echo ""
echo "GPG anahtarı oluşturuluyor (bu biraz zaman alabilir)..."
echo ""

# Anahtar oluştur
if gpg --batch --generate-key "$BATCH_FILE" 2>&1; then
    rm "$BATCH_FILE"
    echo ""
    echo -e "${GREEN}✅ GPG anahtarı başarıyla oluşturuldu!${NC}"
else
    rm "$BATCH_FILE"
    echo -e "${RED}❌ GPG anahtarı oluşturulamadı!${NC}"
    echo "Manuel olarak oluşturmayı deneyin: gpg --full-generate-key"
    exit 1
fi

# Anahtar ID'sini al
KEY_ID=$(gpg --list-secret-keys --keyid-format=long "$EMAIL" | grep sec | awk -F'/' '{print $2}' | awk '{print $1}')

if [ -z "$KEY_ID" ]; then
    echo -e "${RED}❌ Anahtar ID alınamadı!${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}📋 Anahtar ID: $KEY_ID${NC}"

# Git yapılandırması
echo ""
echo "Git yapılandırması yapılıyor..."
git config --global user.signingkey "$KEY_ID"
git config --global commit.gpgsign true
git config --global tag.gpgsign true
git config --global gpg.program gpg

# GPG_TTY ayarı
echo ""
echo "Shell yapılandırması yapılıyor..."
SHELL_RC=""
if [ -f "$HOME/.zshrc" ]; then
    SHELL_RC="$HOME/.zshrc"
elif [ -f "$HOME/.bashrc" ]; then
    SHELL_RC="$HOME/.bashrc"
fi

if [ -n "$SHELL_RC" ]; then
    if ! grep -q "export GPG_TTY" "$SHELL_RC"; then
        echo 'export GPG_TTY=$(tty)' >> "$SHELL_RC"
        echo -e "${GREEN}✅ GPG_TTY eklendi: $SHELL_RC${NC}"
    fi
fi

echo ""
echo -e "${GREEN}✅ Git başarıyla yapılandırıldı!${NC}"
echo ""
echo "============================================"
echo "🎉 Kurulum tamamlandı!"
echo "============================================"
echo ""
echo "Sonraki adımlar:"
echo ""
echo "1️⃣  Public key'inizi GitHub'a ekleyin:"
echo -e "${BLUE}   gpg --armor --export $KEY_ID${NC}"
echo ""
echo "2️⃣  GitHub Settings → SSH and GPG keys → New GPG key"
echo ""
echo "3️⃣  Test commit yapın:"
echo -e "${BLUE}   git commit -S -m \"Test commit\"${NC}"
echo ""
echo "4️⃣  İmzayı doğrulayın:"
echo -e "${BLUE}   git log --show-signature -1${NC}"
echo ""
echo "5️⃣  Shell'i yeniden yükleyin:"
echo -e "${BLUE}   source $SHELL_RC${NC}"
echo ""
echo "📚 Detaylı bilgi için: GPG-SETUP.md"
echo ""

# Public key'i göster
echo "🔑 Public Key (GitHub'a eklemek için kopyalayın):"
echo "--------------------------------------------"
gpg --armor --export "$KEY_ID"
echo "--------------------------------------------"
echo ""

# Yedekleme hatırlatması
echo -e "${YELLOW}⚠️  ÖNEMLİ: Anahtarınızı yedeklemeyi unutmayın!${NC}"
echo ""
echo "Yedekleme komutları:"
echo -e "${BLUE}  gpg --export-secret-keys --armor $KEY_ID > private-key-backup.asc${NC}"
echo -e "${BLUE}  gpg --export --armor $KEY_ID > public-key-backup.asc${NC}"
echo ""
