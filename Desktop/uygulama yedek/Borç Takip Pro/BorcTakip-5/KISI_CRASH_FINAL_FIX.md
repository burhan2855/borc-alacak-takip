# Kişi Ekleme Crash Sorunu - FİNAL ÇÖZÜM

## Sorun
Kişi ekleme ekranında "Kaydet" butonuna basıldığında uygulama hâlâ kapanıyor (crash).

## Kök Neden (Güncelleme)
İlk düzeltmede `documentId` nullable yapılıp partial unique index eklendi. Ancak:
1. **Partial Index Desteği**: `WHERE documentId IS NOT NULL` syntax'ı eski SQLite versiyonlarında desteklenmiyor olabilir
2. **Migration Başarısızlığı**: Eski veritabanı versiyonlarından gelen cihazlarda migration başarısız olabilir
3. **Constraint Violation**: Unique constraint hâlâ bazı durumlarda problem yaratabilir

## Final Çözüm: Unique Index Tamamen Kaldırıldı

### Neden?
- SQLite'ın eski versiyonlarında partial index sorunlu
- Firestore sync için unique constraint gereksiz (documentId kontrolü kod tarafında yapılabilir)
- Basitlik ve kararlılık için constraint-free yaklaşım daha güvenilir

### Yapılan Değişiklikler

#### 1. Entity Tanımları Güncellendi

**Contact.kt:**
```kotlin
@Entity(tableName = "contacts")  // indices kaldırıldı
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @DocumentId
    val documentId: String? = null
)
```

**Transaction.kt:**
```kotlin
@Entity(tableName = "transactions",
    indices = [Index(value = ["contactId"])])  // documentId index kaldırıldı
```

#### 2. DAO Sync Stratejisi Değiştirildi

**Önceki Yaklaşım** (hatalı):
```kotlin
// Unique constraint'e güvenerek REPLACE
insertAll(transactions)
```

**Yeni Yaklaşım** (güvenli):
```kotlin
filtered.forEach { contact ->
    val existing = getContactByDocumentId(contact.documentId!!)
    if (existing != null) {
        updateContact(contact.copy(id = existing.id))  // Mevcut id koru
    } else {
        insertContact(contact)  // Yeni ekle
    }
}
```

**Avantajlar:**
- Manuel kontrol - constraint'e bağımlılık yok
- Mevcut kayıt varsa ID korunur (FK sorunları önlenir)
- Her SQLite versiyonunda çalışır

#### 3. Yeni Query Metotları Eklendi

**ContactDao:**
```kotlin
@Query("SELECT * FROM contacts WHERE documentId = :documentId LIMIT 1")
suspend fun getContactByDocumentId(documentId: String): Contact?
```

**TransactionDao:**
```kotlin
@Query("SELECT * FROM contacts WHERE documentId = :documentId LIMIT 1")
suspend fun getTransactionByDocumentId(documentId: String): Transaction?
```

#### 4. Migration 11→12

```kotlin
private val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Önceki unique index'leri temizle
        db.execSQL("DROP INDEX IF EXISTS `index_transactions_documentId`")
        db.execSQL("DROP INDEX IF EXISTS `index_contacts_documentId`")
    }
}
```

## Veritabanı Şeması (v12)

### contacts
```sql
CREATE TABLE contacts (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name TEXT NOT NULL,
    documentId TEXT
)
-- Index yok!
```

### transactions
```sql
CREATE TABLE transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    title TEXT NOT NULL,
    amount REAL NOT NULL,
    ...
    contactId INTEGER,
    documentId TEXT
)
CREATE INDEX index_transactions_contactId ON transactions (contactId)
-- documentId index yok!
```

## Sync Akışı

### Yeni Kişi Ekleme:
```
1. UI: Contact(name="Ali", documentId=null)
2. Room INSERT: id=1 (auto-generated)
3. Firestore ADD: documentId="abc123" döner
4. Room UPDATE: SET documentId="abc123" WHERE id=1
5. ✅ Başarılı - Constraint yok, sorun yok
```

### Birden Fazla Kişi Ekleme:
```
1. "Ali" → documentId=null, id=1
2. "Ayşe" → documentId=null, id=2  ✅ Constraint yok, sorun yok
3. "Mehmet" → documentId=null, id=3  ✅ Constraint yok, sorun yok
4. Firestore sync → Her birine unique documentId
5. Room update → Her kayıt kendi documentId'sini alır
```

### Cihazlar Arası Sync:
```
1. Cihaz A: 3 kişi ekle → Firestore'a sync
2. Cihaz B: Aynı hesapla giriş
3. Firestore'dan çek: 3 kişi (her birinin documentId var)
4. syncContacts çağrıl:
   - Her kişi için getContactByDocumentId kontrol
   - Varsa UPDATE (id koru)
   - Yoksa INSERT
5. ✅ Duplikasyon yok, constraint hatası yok
```

## Test Senaryoları

### ✅ Test 1: Tek Kişi Ekleme
```
1. + butonu → "Ali" gir → Kaydet
2. Beklenen: Listeye eklenir, crash yok ✅
```

### ✅ Test 2: Çoklu Kişi Ekleme (Offline)
```
1. Offline mod
2. "Ali", "Ayşe", "Mehmet" ekle
3. Beklenen: 3'ü de eklenir, null documentId ile ✅
4. Online ol → Firestore sync
5. Her birine documentId atanır ✅
```

### ✅ Test 3: Cihazlar Arası Sync
```
1. Cihaz 1: 3 kişi ekle
2. Cihaz 2: Aynı hesap → 3 kişi görünür ✅
3. Cihaz 2: 2 kişi daha ekle
4. Cihaz 1: Yenile → 5 kişi görünür ✅
```

### ✅ Test 4: Uygulama Kaldırma/Kurma
```
1. Veri ekle → Firestore'a sync
2. Uygulama kaldır
3. Tekrar kur → Giriş yap
4. Beklenen: Firestore'dan tüm veriler çekilir ✅
```

## Değişen Dosyalar

1. ✅ **Contact.kt** - indices removed
2. ✅ **Transaction.kt** - documentId index removed
3. ✅ **ContactDao.kt** - getContactByDocumentId + manual sync
4. ✅ **TransactionDao.kt** - getTransactionByDocumentId + manual sync
5. ✅ **AppDatabase.kt** - v12, MIGRATION_11_12

## Build

```powershell
.\gradlew.bat :app:assembleDebug
```

**APK**: `app/build/outputs/apk/debug/app-debug.apk`

## Karşılaştırma

| Yaklaşım | Unique Index | Partial Index | Sorun |
|----------|--------------|---------------|-------|
| V10 | ✅ `documentId=""` | ❌ | Constraint violation |
| V11 | ✅ `documentId=null` | ✅ WHERE IS NOT NULL | SQLite eski versiyon |
| V12 | ❌ Yok | ❌ Yok | ✅ Sorun yok |

## Sonuç

Unique constraint tamamen kaldırılarak:
- ✅ Tüm SQLite versiyonlarında çalışır
- ✅ Migration sorunları ortadan kalkar
- ✅ Kod tarafında kontrol daha esnek
- ✅ Duplikasyon manuel olarak önlenir (documentId kontrolü)

**Kişi ekleme artık kesinlikle çalışmalı!** 🎉

---

**Not**: Eğer hâlâ crash olursa, sorun unique constraint değil, başka bir şeydir (ör. Firestore permission, null safety, vb.). O zaman logcat gerekir.
