# Takvim Boş - Sorun Çözme Rehberi (2025-12-20)

## Sorunu Teşhis Etme

### Step 1: Logcat Kontrol Et
```bash
adb logcat -s "DB_DUMP" | grep -i "calendar"
```

**Beklenen Output:**
```
===== CalendarViewScreen DEBUG =====
Total transactions: X
After filtering: Y
  ✓ Tx: title='...', category='...', isDebt=...
```

- Eğer `Total transactions: 0` → **Veritabanında işlem yok!**
- Eğer `After filtering: 0` ama `Total transactions: > 0` → **Filtering hepsini kaldırdı!**
- Eğer `After filtering: > 0` ama takvimde görmüyor → **Tarih eşleşmiyor veya UI hatası!**

### Step 2: İşlem Oluştur ve Test Et
1. App aç
2. **"+"** butonuna tıkla (Ana ekrandaki yeşil buton)
3. **Borç ekle** seçini
4. **İsim**: "Test Takvim Borçu"
5. **Tutar**: 1000
6. **Tarih**: **BUGÜNÜN TARİHİNİ SEÇ** (önemli!)
7. **Kaydet**
8. Takvim sekmesine git
9. **Bu ay** seçili olup olmadığını kontrol et
10. İşlem gösterilip gösterilmediğini kontrol et

### Step 3: Logcat Analizi
İşlem oluşturduktan sonra:
```bash
adb logcat -s "DB_DUMP" | tail -20
```

**Beklenen:**
```
=== INSERT TRANSACTION START ===
Transaction successfully saved, now creating calendar event
Calendar event created successfully
```

## Olası Çözümler

### Çözüm 1: Kategori Filtering Sorunu
**Eğer**: Takvim boş ama DB'de işlem var

**Neden**: Tüm işlemlerin kategorisi "Kasa Çıkışı" veya "Banka Çıkışı"

**Çözüm**:
```kotlin
// CalendarViewScreen.kt satır 36'da
val filtered = allTransactions.filter { transaction ->
    // Sadece Kasa/Banka ÇIKIŞI değil istiyoruz
    !(transaction.category == "Kasa Çıkışı" || transaction.category == "Banka Çıkışı")
}
```

### Çözüm 2: Tarih Filtering Sorunu
**Eğer**: İşlem var ama takvimde farklı ay/yılda

**Neden**: İşlem tarihi seçili ay/yılla eşleşmiyor

**Test**:
- Takvimde "Nisan" gösteriliyor ama işlem "Mart"'a eklediysen takvim boş olur
- **Ay/Yıl seçici kontrol et!**

### Çözüm 3: Veritabanında Hiç İşlem Yok
**Eğer**: Total transactions = 0

**Neden**: Hiç işlem oluşturulmamış

**Çözüm**:
1. **"+"** butonuna tıkla
2. Test işlemi oluştur
3. Takvim sekmesine geri dön

## Debug Logları Detaylı Açıklaması

```
===== CalendarViewScreen DEBUG =====
```
- Takvim ekranı açıldığında tetiklenir
- Tüm işlemleri listeleyecek

```
Total transactions: 10
After filtering: 8
```
- 10 işlem var
- 8'i takvimde gösterilir (2'si filtrelenmiş - muhtemelen Kasa/Banka Çıkışı)

```
  ✓ Tx: title='Borçu Test', category=null, isDebt=true, status='Ödenmedi'
```
- Işlem adı
- Kategori (null = normal borç/alacak)
- İsDebt = true → borç
- Status = 'Ödenmedi' → hala aktif

## Takvim Grid Detayları

```
┌────────────────────────┐
│ Takvim (Toplam: 8)     │ ← Bu sayı güncelleniyor
├────────────────────────┤
│  Ay/Yıl Seçimi         │
│  <  Aralık 2024  >     │ ← Bu önemli! Doğru ay/yıl mı?
├────────────────────────┤
│  P  S  Ç  P  C  C  P   │
│  1  2  3  4  5  6  7   │
│  8  9 10 11 12 13 14   │
│ 15 16 17 18 19 20 21   │
│ 22 23 24 25 26 27 28   │
│ 29 30 31               │
└────────────────────────┘
Renkli günler = İşlem içeriyor
```

## Önemli Kontrol Listesi

- [ ] Takvim sekmesine girdiğimde başlık "Takvim (Toplam: X)" gösterilmiş mi?
- [ ] Ay/Yıl seçimi doğru mu? (Şu anda hangi ay/yıl seçili?)
- [ ] İşlem oluşturduktan sonra takvim sekmesine geri gittim mi? (Refresh gerekebilir)
- [ ] İşlem tarihini seçerken bu ay'ı seçtim mi?
- [ ] Logcat'ta "Total transactions: 0"  mı yoksa > 0 mi?
- [ ] Logcat'ta "After filtering: 0" mı yoksa > 0 mi?

## Hızlı Çözüm

Eğer takvim boş gösteriliyorsa:

1. **Logcat aç**: `adb logcat -s "DB_DUMP"`
2. **"+" tuşuna tıkla** ve test işlemi oluştur
3. **Takvim sekmesine git**
4. **Logcatı oku** - hangi seviyede sorun olduğunu göreceksin
5. **Dosyalama kontrol listesi**ni takip et

## Not

Kod şu anda **production-ready** değildir. Debug logging çok detaylı. Production'da kaldırılmalı.

Test işlemleri için takvime bakıyorsan:
- Ay seçmek için şaşı/sağ kaydır
- İşlem görmek için "Bu ay işlemleri:" bölümüne bak
- Takvim hücreleri işlem içeren günleri vurgular

---
**Sorun Durum**: 🔴 **Araştırılıyor** - Build ve test sonrası güncellenecek
