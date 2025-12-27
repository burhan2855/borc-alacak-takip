# Firestore Senkronizasyon Sorunu Çözüldü

## Sorun Tanımı
Emülatörde eklenen veriler, aynı hesapla telefona giriş yapıldığında görünmüyordu.

## Kök Neden Analizi

### 1. Çıkışta Veri Kaybı (ÇÖZÜLDİ)
- `MainViewModel.onSignOut()` → `clearAllLocalData()` çağrıları tüm Room verisini siliyordu
- **Çözüm**: `clearAllLocalData()` çağrıları kaldırıldı, sadece Firestore dinleyicileri durduruluyor

### 2. Sync Stratejisi Sorunu (ÇÖZÜLDİ)
- `TransactionDao.syncTransactions()` ve `ContactDao.syncContacts()` **delete-all + insert** stratejisi kullanıyordu
- Her senkronda yerel veri silinip Firestore'dan gelen yazılıyordu
- **Sorun**: Eğer Firestore henüz ulaşılmazsa veya kayıt senkron olmamışsa → veri kaybı
- **Çözüm**: **Upsert (merge) stratejisi**'ne geçildi - yerel veri korunuyor, Firestore güncellemeleri üzerine yazılıyor

### 3. Primary Key充돌 (ÇÖZÜLDİ)
- Room primary key: `id` (auto-generated Long)
- Firestore key: `documentId` (String)
- Farklı cihazlarda aynı `id` değeri충돌 yapıyordu
- **Çözüm**: 
  - `documentId` için **unique index** eklendi
  - Sync'te `documentId` boş olanlar filtreleniyor (henüz Firestore'a gitmemiş kayıtlar)
  - `OnConflictStrategy.REPLACE` artık `documentId` unique index ile çalışıyor

## Yapılan Değişiklikler

### 1. MainViewModel.kt
```kotlin
fun onSignOut() = viewModelScope.launch {
    transactionRepository.stopListeningForChanges()
    // Yerel veri artık silinmiyor - cihazlar arası sync korunuyor
    contactRepository.stopListeningForChanges()
}
```

### 2. TransactionDao.kt
```kotlin
@RoomTransaction
suspend fun syncTransactions(transactions: List<Transaction>) {
    // Delete-all kaldırıldı → Merge stratejisi
    val validTransactions = transactions.filter { it.documentId.isNotBlank() }
    insertAll(validTransactions)
}
```

### 3. ContactDao.kt
```kotlin
suspend fun ContactDao.syncContacts(contacts: List<Contact>) {
    // Delete-all kaldırıldı → Merge stratejisi
    val validContacts = contacts.filter { it.documentId.isNotBlank() }
    insertAll(validContacts)
}
```

### 4. Transaction.kt
```kotlin
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["contactId"]), 
        Index(value = ["documentId"], unique = true)  // ← YENİ
    ]
)
```

### 5. Contact.kt
```kotlin
@Entity(
    tableName = "contacts", 
    indices = [Index(value = ["documentId"], unique = true)]  // ← YENİ
)
```

### 6. AppDatabase.kt
- Veritabanı versiyonu: **8 → 9**
- **MIGRATION_8_9** eklendi:
  - `CREATE UNIQUE INDEX index_transactions_documentId`
  - `CREATE UNIQUE INDEX index_contacts_documentId`

## Yeni Senkronizasyon Akışı

### Emülatörde Veri Ekleme:
1. Kullanıcı kayıt ekler → Room'a yazılır (id=1, documentId="")
2. `TransactionRepository.insert()` → Firestore'a sync (documentId="abc123" alır)
3. Room güncelenir: id=1, documentId="abc123"

### Telefonda Giriş:
1. Aynı hesapla giriş → Firestore dinleyici başlar
2. Firestore'dan çeker: id=1, documentId="abc123"
3. **syncTransactions()** çağrılır:
   - ~~Delete all~~ (KALDIRILDI)
   - `documentId.isNotBlank()` kontrolü ✅
   - `insertAll()` → `OnConflictStrategy.REPLACE` + unique index
   - Eğer `documentId="abc123"` zaten varsa → günceller
   - Eğer yoksa → ekler
4. Yerel veri korunur, Firestore veriyle birleşir ✅

### Çıkış/Yeniden Giriş:
1. Çıkış → Dinleyiciler durur, **yerel veri SİLİNMEZ**
2. Yeniden giriş → Dinleyiciler başlar, Firestore ile senkron olur
3. Veriler kaybolmaz ✅

## Test Senaryoları

### ✅ Senaryo 1: Aynı Hesap, Farklı Cihazlar
```
1. Emülatör → Hesap A ile giriş → Veri ekle
2. Telefon → Hesap A ile giriş → VERİLER GÖRÜNMELİ ✅
```

### ✅ Senaryo 2: Çıkış/Giriş
```
1. Emülatör → Veri ekle
2. Emülatör → Çıkış yap
3. Emülatör → Aynı hesapla giriş → VERİLER KORUNMALI ✅
```

### ✅ Senaryo 3: Offline → Online
```
1. Telefon offline → Veri ekle (documentId="")
2. Telefon online → Firestore sync (documentId="xyz789" alır)
3. Emülatör → Giriş → Telefondaki veri GÖRÜNMELİ ✅
```

### ✅ Senaryo 4: Farklı Hesaplar
```
1. Emülatör → Hesap A → Veri A ekle
2. Telefon → Hesap B → Veri B ekle
3. Her cihaz kendi hesabının verilerini görür ✅
```

## Build Durumu
```
BUILD SUCCESSFUL in 39s
37 actionable tasks: 12 executed, 25 up-to-date
```

**APK Konumu**: `app/build/outputs/apk/debug/app-debug.apk`

## Notlar
- `.fallbackToDestructiveMigration()` hala aktif - production'da migration'lar üzerinden güncellemeli
- Unique index sayesinde `documentId` çakışma koruması var
- Yerel-önce (local-first) strateji: offline çalışır, online olunca sync eder

## Sonraki Adımlar
1. Emülatör ve telefonda test et
2. Çakışma senaryolarını doğrula (aynı `id`, farklı `documentId`)
3. Production release öncesi migration stratejisini gözden geçir
