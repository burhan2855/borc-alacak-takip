# Kişi Kaydederken Uygulama Kapanma Sorunu - ÇÖZÜLDÜ

## Sorun
Kişi ekleme ekranında "Kaydet" butonuna basıldığında uygulama crash oluyor.

## Kök Neden
`documentId` alanı için unique index vardı ancak varsayılan değer boş string (`""`) idi. 

**Senaryo:**
1. İlk kişi eklendiğinde: `Contact(id=1, name="Ali", documentId="")`
2. İkinci kişi eklendiğinde: `Contact(id=2, name="Ayşe", documentId="")`
3. **CRASH**: Unique constraint violation - iki kayıt da `documentId=""` ile yazılmaya çalışıldı

Firestore'a senkronize edilene kadar (`documentId = null` veya boş), unique index çakışma yapıyordu.

## Çözüm

### 1. documentId Nullable Yapıldı

**Contact.kt:**
```kotlin
@DocumentId
val documentId: String? = null  // Artık nullable
```

**Transaction.kt:**
```kotlin
@DocumentId
var documentId: String? = null  // Artık nullable
```

### 2. Partial Unique Index (WHERE IS NOT NULL)

**Migration 10->11:**
- `documentId` NULL değerleri unique constraint'ten muaf
- Sadece Firestore'dan gelen (non-null) `documentId` değerleri unique kontrole tabi
- Boş/null kayıtlar çakışmadan yazılabiliyor

```sql
CREATE UNIQUE INDEX index_contacts_documentId 
ON contacts (documentId) 
WHERE documentId IS NOT NULL
```

### 3. Sync Fonksiyonları Güncellendi

**ContactDao.syncContacts:**
```kotlin
val validContacts = contacts.filter { !it.documentId.isNullOrBlank() }
```

**TransactionDao.syncTransactions:**
```kotlin
val filtered = transactions.filter { !it.documentId.isNullOrBlank() }
```

## Veritabanı Migration

**Version: 10 → 11**

- `transactions` tablosu yeniden oluşturuldu (documentId nullable)
- `contacts` tablosu yeniden oluşturuldu (documentId nullable)
- Partial unique index'ler eklendi (WHERE documentId IS NOT NULL)
- Eski boş string değerler NULL'a dönüştürüldü

## Akış

### Yeni Kişi Ekleme:
```
1. Kullanıcı "Ali" girer → Kaydet
2. Room: Contact(id=0, name="Ali", documentId=null) → INSERT
3. Room: newId=1 döndürür
4. Firestore: Contact'ı ekler → documentId="abc123" döner
5. Room: UPDATE Contact SET documentId="abc123" WHERE id=1
6. ✅ Başarılı
```

### Birden Fazla Kişi Ekleme:
```
1. "Ali" ekle → documentId=null (geçici)
2. "Ayşe" ekle → documentId=null (geçici) ✅ ÇAKIŞMA YOK
3. Firestore sync → Her birine unique documentId atanır
4. Room update → documentId'ler unique olarak yazılır
```

## Test Senaryoları

### ✅ Test 1: Tek Kişi Ekleme
```
1. Kişiler ekranında + butonuna tıkla
2. İsim gir: "Ali"
3. Kaydet
4. Beklenen: Kişi listeye eklenir, uygulama kapanmaz ✅
```

### ✅ Test 2: Çoklu Kişi Ekleme
```
1. "Ali" ekle
2. "Ayşe" ekle
3. "Mehmet" ekle
4. Beklenen: Üçü de listede görünür ✅
```

### ✅ Test 3: Offline/Online
```
1. Offline modda 2 kişi ekle
2. Online ol
3. Firestore sync çalışır
4. Beklenen: Her kişiye documentId atanır ✅
```

### ✅ Test 4: Cihazlar Arası Sync
```
1. Cihaz 1: 3 kişi ekle
2. Cihaz 2: Aynı hesapla giriş
3. Beklenen: 3 kişi görünür ✅
```

## Değişen Dosyalar

1. **Contact.kt**: `documentId` nullable
2. **Transaction.kt**: `documentId` nullable
3. **ContactDao.kt**: Sync fonksiyonu null-safe
4. **TransactionDao.kt**: Sync fonksiyonu null-safe
5. **AppDatabase.kt**: 
   - Version 11
   - Migration 10->11 eklendi
   - Partial unique index

## Logcat Kontrol

Başarılı kişi ekleme:
```
D/ContactRepo: Inserting contact: Ali
D/ContactRepo: Contact inserted to Room with ID: 1
D/ContactRepo: Syncing contact to Firestore...
D/ContactRepo: Firestore document created: abc123xyz
```

## Notlar

- `.fallbackToDestructiveMigration()` aktif olduğu için eski veritabanı sıfırlanabilir
- Migration başarısız olursa veri kaybolabilir (production'da migration'ları test edin)
- `documentId` nullable olduğu için Kotlin null-check'leri gerekebilir (ör. `?.isNotBlank()`)

## Build Komutu

```powershell
.\gradlew.bat clean :app:assembleDebug
```

**APK Konumu**: `app/build/outputs/apk/debug/app-debug.apk`

---

**Sonuç**: Artık kişi ekleme çalışıyor, uygulama kapanmıyor. Birden fazla kişi eklenebiliyor ve Firestore senkronizasyonu sorunsuz çalışıyor. 🎉
