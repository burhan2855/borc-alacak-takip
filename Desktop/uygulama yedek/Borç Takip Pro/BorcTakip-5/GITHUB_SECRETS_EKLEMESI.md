# 🔐 GitHub Secrets - YENİ KEYSTORE İLE KURULUM

**Tarih:** 27 Aralık 2025  
**Durum:** ✅ YENİ KEYSTORE OLUŞTURULDU

---

## 📋 GİTHUB'DA EKLENECEK 4 SECRET

### ⚠️ ÖNEMLİ: Aşağıdaki değerleri eklemeden önce bu rehberi dikkatlice okuyun!

---

## 🔐 SECRET 1: BORC_TAKIP_STORE_PASSWORD

```
Name: BORC_TAKIP_STORE_PASSWORD
Value: BorcTakip2024Secure!
```

**Açıklama:** Keystore dosyasının ana şifresi

**Talimatlar:**
1. GitHub → Repository Settings → Secrets and variables → Actions
2. "New repository secret" butonuna tıklayın
3. Name alanına: `BORC_TAKIP_STORE_PASSWORD` yazın
4. Value alanına: `BorcTakip2024Secure!` yapıştırın
5. "Add secret" butonuna tıklayın

---

## 🔐 SECRET 2: BORC_TAKIP_KEY_ALIAS

```
Name: BORC_TAKIP_KEY_ALIAS
Value: release-key
```

**Açıklama:** Keystore içindeki key'in adı

**Talimatlar:**
1. "New repository secret" butonuna tıklayın
2. Name alanına: `BORC_TAKIP_KEY_ALIAS` yazın
3. Value alanına: `release-key` yazın
4. "Add secret" butonuna tıklayın

---

## 🔐 SECRET 3: BORC_TAKIP_KEY_PASSWORD

```
Name: BORC_TAKIP_KEY_PASSWORD
Value: BorcTakip2024Secure!
```

**Açıklama:** Key'in şifresi (aynı keystore password)

**Talimatlar:**
1. "New repository secret" butonuna tıklayın
2. Name alanına: `BORC_TAKIP_KEY_PASSWORD` yazın
3. Value alanına: `BorcTakip2024Secure!` yapıştırın
4. "Add secret" butonuna tıklayın

---

## 🔐 SECRET 4: SIGNING_KEY

```
Name: SIGNING_KEY
Value: [Base64 CODED KEYSTORE - Clipboard'da]
```

**Açıklama:** Base64 kodlanmış keystore dosyası (release build için)

**Talimatlar:**
1. "New repository secret" butonuna tıklayın
2. Name alanına: `SIGNING_KEY` yazın
3. Value alanına: Clipboard'daki base64 string'i yapıştırın
4. "Add secret" butonuna tıklayın

---

## 📍 GITHUB'DA SECRET EKLEME ADIM ADIM

### ADIM 1: Repository'ye Gidin
```
https://github.com/burhan2855/borctakip
```

### ADIM 2: Settings Tab'ını Tıklayın
Sağ üst köşede "Settings" linkini tıklayın

### ADIM 3: Secrets Menüsü
Sol menüde:
1. "Secrets and variables" seçeneğini bulun
2. "Actions" sub-section'u tıklayın

### ADIM 4: Secret Ekle
"New repository secret" yeşil butonunu tıklayın

### ADIM 5: İlk Secret'i Ekle
```
Name: BORC_TAKIP_STORE_PASSWORD
Value: BorcTakip2024Secure!
```
"Add secret" butonuna tıklayın

### ADIM 6: İkinci Secret'i Ekle
```
Name: BORC_TAKIP_KEY_ALIAS
Value: release-key
```
"Add secret" butonuna tıklayın

### ADIM 7: Üçüncü Secret'i Ekle
```
Name: BORC_TAKIP_KEY_PASSWORD
Value: BorcTakip2024Secure!
```
"Add secret" butonuna tıklayın

### ADIM 8: Dördüncü Secret'i Ekle
```
Name: SIGNING_KEY
Value: [Base64 string - Clipboard'dan yapıştır]
```
"Add secret" butonuna tıklayın

---

## ✅ TAMAMLAMA KONTROL LİSTESİ

Secret'leri ekledikten sonra kontrol edin:

- [ ] BORC_TAKIP_STORE_PASSWORD ✅
- [ ] BORC_TAKIP_KEY_ALIAS ✅
- [ ] BORC_TAKIP_KEY_PASSWORD ✅
- [ ] SIGNING_KEY ✅

Eğer 4 adet secret görmüşseniz, tamamlandı! ✅

---

## 🚀 SECRETS EKLENDIKTEN SONRA

Secrets eklendikten 1 dakika sonra:

```bash
git push origin develop
```

Bu komut GitHub Actions'ı tetikleyecek:
- ✅ android-build.yml çalışacak
- ✅ Debug APK build yapılacak
- ✅ Unit test çalışacak

**GitHub → Actions sekmesine bakın**
- Yeşil ✅ check görmeli misiniz?
- Kırmızı ❌ görmüş müyüz? Logları kontrol edin

---

## 📝 LOKAL DOSYA BİLGİSİ

Lokal makinenizde:

```
Keystore Dosyası: release-key.keystore
Yedek Dosya: release-key.keystore.backup (eski keystore)
```

**Bu dosyalar repository'ye COMMIT ETMEYIN!**
`.gitignore`'da zaten var ama kontrol edin.

---

## 🔑 ŞIFRE ÖZET

Hızlı referans (yazıp saklayın):

| Anahtar | Değer |
|---------|-------|
| **Keystore Şifresi** | `BorcTakip2024Secure!` |
| **Key Alias** | `release-key` |
| **Key Şifresi** | `BorcTakip2024Secure!` |
| **Keystore Dosyası** | `release-key.keystore` |
| **Geçerlilik** | 10000 gün (~27 yıl) |

---

## ⚠️ ÖNEMLİ NOTLAR

1. **Şifreleri güvenli bir yerde saklayın**
   - Txt dosyasına yazıp şifreleyin
   - Password manager'a ekleyin
   - Başkasına söylemeyin

2. **SIGNING_KEY Base64 String**
   - Clipboard'da kopyalanmıştır
   - GitHub Secret'e direkt yapıştırıp "Add secret" tıklayın
   - Boş satır kalmasın

3. **Keystore Dosyası**
   - GitHub'a PUSH ETMEYİN
   - `.gitignore`'da zaten kapalı
   - Lokal makinede saklı

4. **Release Build**
   - SIGNING_KEY ekledikten sonra tag ile release build yapabilir
   - `git tag v1.0.0` ve `git push origin v1.0.0`
   - GitHub Actions otomatik release APK build'leyecek

---

## 📞 SORUN GİDERME

### Sorun: Secret'ler listelenmemiş

**Çözüm:** Sayfayı yenile (F5)

### Sorun: "Add secret" butonu gri

**Çözüm:** Tüm alanları doldurduğunuz kontrol edin

### Sorun: Base64 string çalışmıyor

**Çözüm:** Tüm string'i kopyaladığınız kontrol edin

### Sorun: Build başarısız

**Çözüm:** 
- Actions sekmesinde logları okuyun
- Secret adlarını kontrol edin (case-sensitive!)
- `./gradlew :app:assembleRelease` lokal'de test edin

---

## ✅ SONUÇ

4 adet GitHub Secret ekledikten sonra:

✅ Debug builds otomatik çalışacak  
✅ Test'ler otomatik çalışacak  
✅ Lint otomatik çalışacak  
✅ Release builds imzalı olacak  

---

**🎉 GitHub Secrets kurulumu için hazır!**

**Şimdi yapacak:** GitHub'da 4 secret'i ekleyin!

---

*Oluşturma Tarihi: 27 Aralık 2025*  
*Durum: ✅ Hazır*  
*Sonraki: GitHub'da Secrets ekle*
