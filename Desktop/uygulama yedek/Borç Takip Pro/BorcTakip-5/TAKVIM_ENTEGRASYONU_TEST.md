# Takvim Entegrasyonu Test Protokolü

## Amaç
Takvim entegrasyonunun düzgün çalışıp çalışmadığını test etmek.

## Ön Şartlar

### 1. Emulatörde Hazırlık
- [ ] Emulatörü başlat (`emulator -avd <avd_name>`)
- [ ] Settings > Accounts > Add Account
  - [ ] Google seç
  - [ ] Bir Google hesabı ile giriş yap (example@gmail.com gibi)
- [ ] Google Play Store açarak sinkronizasyon sağla
- [ ] Calendar uygulamasını başlat
  - [ ] Herhangi bir ay'a tıkla (sağlayıcı etkinleşecek)
- [ ] Calendar uygulamasını kapat

### 2. Uygulamayı Kur
- [ ] Terminal açarak: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
- [ ] Uygulamayı başlat

### 3. İlk Açılışta İzin Ver
- [ ] Calendar permission dialog'u görüldüğünde "Allow" tıkla
- [ ] Ana ekrana gel

---

## Test Senaryoları

### Senaryo 1: Borç Ekleme ve Takvime Eklenme

**Adımlar:**
1. Ana ekranda "+" butonuna tıkla (veya "Borç Ekle")
2. Aşağıdaki bilgileri gir:
   - **Kişi:** "Test" (yeni kişi olarak ekle)
   - **Başlık:** "Test Borç - Takvim 1"
   - **Tutar:** 1000
   - **Tarih:** Bugün (default)
   - **Durum:** "Ödenmedi"
3. "Kaydet" butonuna tıkla
4. Hata dialog'u check et - hata yoksa işlem başarılı

**Beklenen Sonuç:**
- ✅ İşlem kaydedildi
- ✅ Takvim etkinliği oluşturuldu (logcat'te "Calendar event created successfully" görülmeli)

**Kontrol (Logcat):**
```
adb logcat -s "DB_DUMP" | grep -i "calendar"
```

Şunu görmeli:
```
D/DB_DUMP: Creating calendar event for transaction ID: 1
D/DB_DUMP: Calendar event created successfully
```

---

### Senaryo 2: Alacak Ekleme ve Takvime Eklenme

**Adımlar:**
1. Ana ekranda "+" butonuna tıkla
2. "Alacak Ekle" seçeneğini seç
3. Aşağıdaki bilgileri gir:
   - **Kişi:** "Test"
   - **Başlık:** "Test Alacak - Takvim 2"
   - **Tutar:** 5000
   - **Tarih:** Yarın
   - **Durum:** "Tahsil Edilmedi"
4. "Kaydet" tıkla

**Beklenen Sonuç:**
- ✅ Alacak kaydedildi
- ✅ Takvim etkinliği oluşturuldu

---

### Senaryo 3: Takvim Ekranında Görüntüleme

**Adımlar:**
1. Ana ekrandan "Takvim" sekmesine git
2. Ay seçici ile takvim'e bak

**Beklenen Sonuç:**
- ✅ Eklenen işlemlerin tarihleri göstergeleriyle işaretlenmiş (mor nokta)
- ✅ "Bu Aydaki Etkinlikler" listesinde:
  - "Test Borç - Takvim 1"
  - "Test Alacak - Takvim 2"
  - (varsa diğer işlemler)

---

### Senaryo 4: İşlem Silme Sonrası Takvim

**Adımlar:**
1. "Borclar" sekmesine git
2. "Test Borç - Takvim 1" işlemine tıkla
3. "Sil" butonuna tıkla
4. Onay dialogu'nda "Sil" tıkla
5. Takvim sekmesine dön

**Beklenen Sonuç:**
- ✅ Silinen işlem artık takvimde görünmüyor
- ✅ Logcat'te takvim event deletion logları görülüyor

---

### Senaryo 5: Ödeme Yapıldığında Takvim Durumu Güncellemesi

**Adımlar:**
1. Ana ekrandan "Borclar" sekmesine git
2. Kalan borçlardan birine tıkla
3. "Ödeme Yap" seçeneğine tıkla
4. "Kasadan Ödeme" veya "Bankadan Ödeme" seç
5. Tutar gir ve tarih seç
6. "Kaydet" tıkla
7. İşlem durumunu check et
8. Takvim'e dön

**Beklenen Sonuç:**
- ✅ Ödeme işlemi kaydedildi
- ✅ Takvim'de işlemin durumu "Ödendi" olarak görüntülenmiş

---

## Hata Yönetimi

### Eğer Hata Oluşursa:

**Hata 1: FOREIGN KEY constraint failed**
```
Takvim entegrasyonu test başarısız: ... 
FOREIGN KEY constraint failed (code 787)
```

**Çözüm:**
- Emulatörü tamamen kapatıp, uygulamayı sil ve tekrar kur
- Settings > Apps > BorçTakip > Storage > Clear Data

**Hata 2: Calendar permissions not granted**
```
D/DB_DUMP: ERROR: Calendar permissions not granted
```

**Çözüm:**
- Settings > Apps > BorçTakip > Permissions
- Calendar: Allow (sağladıktan sonra uygulamayı restart et)

**Hata 3: No writable calendar found**
```
D/DB_DUMP: ERROR: No writable calendar found
```

**Çözüm:**
- Emulatörde Google hesabı kurmuş mısın? Kontrol et
- Calendar uygulamasını bir kez aç ve kapat
- Emulatörü restart et

---

## Logcat Komutu

Takvim spesifik logları görmek için:

```bash
# Terminal veya PowerShell'de:
adb logcat -s "DB_DUMP" -d | findstr /i "calendar"

# Linux/Mac:
adb logcat -s "DB_DUMP" -d | grep -i calendar
```

---

## Test Sonuç Raporu

- [ ] Senaryo 1: PASS / FAIL
- [ ] Senaryo 2: PASS / FAIL
- [ ] Senaryo 3: PASS / FAIL
- [ ] Senaryo 4: PASS / FAIL
- [ ] Senaryo 5: PASS / FAIL

**Genel Sonuç:** 
- [ ] ✅ TÜM TESTLER BAŞARILI
- [ ] ⚠️ KISMÎ HATALAR (hangileri?)
- [ ] ❌ CIDDI HATALAR (hangileri?)

**Notlar:**
```
(Burada test sırasında oluşan hata mesajlarını ve gözlemlerinizi yazın)
```

---

## Bilinen Kısıtlamalar

1. **Emulatör vs Gerçek Cihaz:**
   - Emulatörde Google Play Services sınırlı olabilir
   - Gerçek cihazda hiç sorun olmayacak

2. **Takvim İçerik:**
   - Takvim etkinlikleri "Gizlilik Modu" açıksa sadece saat gösterilir
   - İşletme detayları gizlenir

3. **Sinkronizasyon:**
   - Firestore sinkronizasyon hatalarından bağımsız çalışır
   - Lokal takvim çalışır bile Firestore'a yazma başarısız olsa

---

## İletişim

Test sırasında hata yaşanırsa:
- Logcat çıktısını kaydet: `adb logcat -s "DB_DUMP" -d > takvim_logs.txt`
- screenshots al
- Problemi açıkla
