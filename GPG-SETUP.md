# GPG Anahtar Oluşturma ve Git İmzalama Kılavuzu

Bu kılavuz, GPG anahtar çifti oluşturma ve Git commit'lerini imzalama sürecini adım adım açıklamaktadır.

## GPG Nedir?

GPG (GNU Privacy Guard), dijital imzalar ve şifreleme için kullanılan açık kaynaklı bir araçtır. Git commit'lerinizi imzalayarak, değişikliklerin gerçekten sizin tarafınızdan yapıldığını doğrulayabilirsiniz.

## Gereksinimler

- GPG kurulumu (çoğu Linux dağıtımında varsayılan olarak yüklüdür)
- Git yüklü olmalıdır

## 1. GPG Anahtarı Oluşturma

Yeni bir GPG anahtar çifti oluşturmak için aşağıdaki komutu çalıştırın:

```bash
gpg --full-generate-key
```

### Adımlar:

1. **Anahtar türünü seçin**: Varsayılan seçenek olan "RSA and RSA" (1) önerilir.
2. **Anahtar boyutunu belirleyin**: En az 4096 bit önerilir (güvenlik için).
3. **Anahtar geçerlilik süresini belirleyin**: İhtiyaçlarınıza göre seçin (örn: 0 = sona ermez, 1y = 1 yıl).
4. **Kimlik bilgilerinizi girin**:
   - Ad ve soyad
   - E-posta adresi (GitHub hesabınızda kullandığınız e-posta)
   - Yorum (opsiyonel)
5. **Güçlü bir parola belirleyin**: Bu parola anahtarınızı koruyacaktır.

### Örnek Oturum:

```
gpg (GnuPG) 2.4.4; Copyright (C) 2024 g10 Code GmbH
This is free software: you are free to change and redistribute it.

Please select what kind of key you want:
   (1) RSA and RSA
   (2) DSA and Elgamal
   (3) DSA (sign only)
   (4) RSA (sign only)
   (9) ECC (sign and encrypt) *default*
  (10) ECC (sign only)
  (14) Existing key from card
Your selection? 1

RSA keys may be between 1024 and 4096 bits long.
What keysize do you want? (3072) 4096

Please specify how long the key should be valid.
         0 = key does not expire
      <n>  = key expires in n days
      <n>w = key expires in n weeks
      <n>m = key expires in n months
      <n>y = key expires in n years
Key is valid for? (0) 0

Is this correct? (y/N) y

GnuPG needs to construct a user ID to identify your key.

Real name: Kullanıcı Adı
Email address: kullanici@example.com
Comment: GitHub Signing Key
You selected this USER-ID:
    "Kullanıcı Adı (GitHub Signing Key) <kullanici@example.com>"

Change (N)ame, (C)omment, (E)mail or (O)kay/(Q)uit? O
```

## 2. GPG Anahtarınızı Listeleme

Oluşturduğunuz anahtarları görmek için:

```bash
gpg --list-secret-keys --keyid-format=long
```

Çıktı şuna benzer olacaktır:

```
/home/kullanici/.gnupg/secring.gpg
------------------------------------
sec   4096R/AABBCCDD11223344 2025-12-25 [expires: never]
uid                          Kullanıcı Adı (GitHub Signing Key) <kullanici@example.com>
ssb   4096R/5566778899AABBCC 2025-12-25
```

`AABBCCDD11223344` kısmı GPG anahtar ID'nizdır.

## 3. Git'i GPG ile Yapılandırma

### GPG Anahtarınızı Git'e Tanıtma

GPG anahtar ID'nizi kullanarak Git'i yapılandırın:

```bash
git config --global user.signingkey AABBCCDD11223344
```

### Otomatik İmzalamayı Etkinleştirme

Tüm commit'lerin otomatik olarak imzalanması için:

```bash
git config --global commit.gpgsign true
```

### Tag'leri İmzalama

Tag'leri otomatik olarak imzalamak için:

```bash
git config --global tag.gpgsign true
```

## 4. GPG Anahtarını GitHub'a Ekleme

1. GPG public key'inizi dışa aktarın:

```bash
gpg --armor --export AABBCCDD11223344
```

2. Çıktıyı kopyalayın (-----BEGIN PGP PUBLIC KEY BLOCK----- ile başlar)

3. GitHub hesabınıza gidin:
   - Settings → SSH and GPG keys → New GPG key
   - Anahtarı yapıştırın ve kaydedin

## 5. İmzalı Commit Yapma

### Manuel İmzalama

Tek bir commit'i imzalamak için `-S` bayrağını kullanın:

```bash
git commit -S -m "İmzalı commit mesajı"
```

### Otomatik İmzalama

Yukarıdaki yapılandırmayı yaptıysanız, artık normal şekilde commit yapabilirsiniz:

```bash
git commit -m "Commit mesajı"
```

## 6. İmzalı Commit'leri Doğrulama

Bir commit'in imzasını doğrulamak için:

```bash
git log --show-signature -1
```

veya

```bash
git verify-commit HEAD
```

## Sorun Giderme

### "gpg: signing failed: Inappropriate ioctl for device"

Bu hatayı alırsanız, şu komutu çalıştırın:

```bash
export GPG_TTY=$(tty)
```

Bu ayarı kalıcı yapmak için shell yapılandırma dosyanıza ekleyin (~/.bashrc veya ~/.zshrc):

```bash
echo 'export GPG_TTY=$(tty)' >> ~/.bashrc
```

### Parola Girişi Sorunu

GPG agent'ı yeniden başlatmayı deneyin:

```bash
gpgconf --kill gpg-agent
gpgconf --launch gpg-agent
```

### Anahtar Bulunamadı

Git'in doğru GPG programını kullandığından emin olun:

```bash
git config --global gpg.program gpg
```

## Güvenlik İpuçları

1. **Güçlü parola kullanın**: GPG anahtarınız için uzun ve karmaşık bir parola seçin
2. **Anahtarınızı yedekleyin**: Anahtarınızı güvenli bir yerde saklayın
3. **Revocation sertifikası oluşturun**: Anahtarınızı kaybederseniz iptal edebilmeniz için
4. **Private key'i paylaşmayın**: Public key'i paylaşabilirsiniz, ancak private key'i asla
5. **Düzenli olarak yenileyin**: Gerekirse eski anahtarları iptal edip yenilerini oluşturun

## Anahtar Yedekleme

### Private Key'i Yedekleme

```bash
gpg --export-secret-keys --armor AABBCCDD11223344 > private-key-backup.asc
```

**ÖNEMLİ**: Bu dosyayı güvenli bir yerde saklayın!

### Public Key'i Yedekleme

```bash
gpg --export --armor AABBCCDD11223344 > public-key-backup.asc
```

### Anahtarı Geri Yükleme

```bash
gpg --import private-key-backup.asc
gpg --import public-key-backup.asc
```

## Revocation Sertifikası Oluşturma

Anahtarınızı iptal etmeniz gerekirse kullanmak üzere:

```bash
gpg --gen-revoke AABBCCDD11223344 > revoke-certificate.asc
```

## Ek Kaynaklar

- [GitHub GPG Belgelerİ](https://docs.github.com/en/authentication/managing-commit-signature-verification)
- [GnuPG Belgelerİ](https://gnupg.org/documentation/)
- [Git Belgelerİ - Signing](https://git-scm.com/book/en/v2/Git-Tools-Signing-Your-Work)

## Yardım

Bu rehberle ilgili sorularınız veya sorunlarınız için bir issue açabilirsiniz.
