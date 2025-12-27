# Firebase Senkronizasyon Test Talimatları

## 📱 APK Yükleme ve Test

### 1. APK'yı Yükleyin
```bash
adb install -r "app/build/outputs/apk/debug/app-debug.apk"
```

### 2. Detaylı Logcat Başlatın
Yeni terminal açıp şu komutu çalıştırın:
```bash
adb logcat -c
adb logcat | findstr /C:"DebtApplication" /C:"TransactionRepo" /C:"ContactRepo" /C:"MainViewModel"
```

### 3. Uygulamayı Başlatın ve Log'ları Kontrol Edin

#### Beklenen Log Sırası (Uygulama Başlangıcında):
```
DebtApplication: Application onCreate called
DebtApplication: User signed in: [email], starting Firebase sync
MainViewModel: === INITIALIZING DATA SYNC ===
MainViewModel: Starting TransactionRepository listener...
TransactionRepo: startListeningForChanges called
TransactionRepo: Current user ID: [user-id]
TransactionRepo: Transactions collection: [collection-path]
TransactionRepo: Firestore listener registered successfully for transactions
ContactRepo: startListeningForChanges called
ContactRepo: Current user ID: [user-id]
ContactRepo: Contacts collection: [collection-path]
ContactRepo: Firestore listener registered successfully for contacts
MainViewModel: === DATA SYNC INITIALIZED ===
```

### 4. Google ile Giriş Yapın

Eğer giriş yapmadıysanız:
1. "Google ile Giriş Yap" butonuna tıklayın
2. Hesabınızı seçin
3. Log'ları kontrol edin - yukarıdaki mesajları görmelisiniz

### 5. Veri Ekleme Testi

#### Test 1: Yeni Borç Ekleme
1. Ana ekranda "+" butonuna tıklayın
2. "Borç Ekle" seçin
3. Bilgileri doldurun ve kaydedin

**Beklenen Log:**
```
DB_DUMP: === INSERT TRANSACTION START ===
DB_DUMP: Transaction Title: [başlık]
TransactionRepo: Inserting transaction...
TransactionRepo: Transaction inserted to Room with ID: [id]
TransactionRepo: Syncing to Firestore...
TransactionRepo: Firestore document created: [document-id]
DB_DUMP: === INSERT TRANSACTION SUCCESS ===
```

#### Test 2: Contact Ekleme
1. Contacts ekranına gidin
2. Yeni contact ekleyin

**Beklenen Log:**
```
ContactRepo: Inserting contact: [isim]
ContactRepo: Contact inserted to Room with ID: [id]
ContactRepo: Syncing contact to Firestore...
ContactRepo: Firestore document created: [document-id]
```

### 6. Firebase Senkronizasyon Testi

#### Firebase'den Veri Çekme:
Log'larda şunları arayın:
```
TransactionRepo: Received [X] transactions from Firestore
TransactionRepo: Synced [X] transactions to local DB
ContactRepo: Received [X] contacts from Firestore
ContactRepo: Synced [X] contacts to local DB
```

### 7. Çıkış ve Tekrar Giriş Testi

1. **Çıkış Yapın:**
   - Ayarlar > Çıkış Yap
   
   **Beklenen Log:**
   ```
   DebtApplication: No user signed in, stopping Firebase sync
   TransactionRepo: stopListeningForChanges called
   ContactRepo: stopListeningForChanges called
   ```

2. **Tekrar Giriş Yapın:**
   - Google ile giriş yapın
   
   **Beklenen Log:**
   ```
   DebtApplication: User signed in: [email], starting Firebase sync
   MainViewModel: === INITIALIZING DATA SYNC ===
   TransactionRepo: startListeningForChanges called
   TransactionRepo: Firestore listener registered successfully
   ContactRepo: startListeningForChanges called  
   ContactRepo: Firestore listener registered successfully
   TransactionRepo: Received [X] transactions from Firestore
   TransactionRepo: Synced [X] transactions to local DB
   ContactRepo: Received [X] contacts from Firestore
   ContactRepo: Synced [X] contacts to local DB
   ```

3. **Verilerinizi Kontrol Edin:**
   - Ana ekrana gidin
   - Daha önce eklediğiniz tüm borç/alacakları görmelisiniz
   - Contacts ekranında tüm contact'larınızı görmelisiniz

### 8. Sorun Giderme

#### Problem: "Cannot start listener - user not signed in" mesajı
**Çözüm:** Firebase Auth'un tam olarak tamamlanmasını bekleyin. Bu mesaj normal bir geçiş durumudur.

#### Problem: "Firestore listener error: PERMISSION_DENIED"
**Çözüm:** 
1. Firebase Console > Firestore Database > Rules
2. Şu kuralları ekleyin:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

#### Problem: Veriler Firebase'e gitmiyor
**Kontrol Listesi:**
- [ ] İnternet bağlantısı var mı?
- [ ] Firebase console'da proje doğru mu?
- [ ] `google-services.json` dosyası güncel mi?
- [ ] Log'larda hata mesajı var mı?

#### Problem: Tekrar giriş yapınca veriler gelmiyor
**Çözüm:**
1. Firebase Console'a gidin
2. Firestore Database > Data
3. `users/[your-user-id]/transactions` yolunu kontrol edin
4. Veriler orada varsa, log'larda "Firestore listener error" arayın

### 9. Firebase Console Kontrolü

1. https://console.firebase.google.com adresine gidin
2. Projenizi seçin
3. Firestore Database > Data
4. Şu yolu kontrol edin: `users/[user-id]/`
   - `transactions` koleksiyonu
   - `contacts` koleksiyonu

Her işlem için bir document olmalı, her document'te:
- `id`: Room database ID
- `documentId`: Firestore document ID
- Diğer alanlar (title, amount, date, vb.)

### 10. Başarı Kriterleri

✅ **Giriş Testi:** Auth listener mesajları görünmeli  
✅ **Senkronizasyon Başlatma:** Her iki repository için listener kayıt mesajları  
✅ **Veri Ekleme:** Firebase'e senkronizasyon mesajları  
✅ **Veri Çekme:** "Received X items from Firestore" mesajları  
✅ **Çıkış:** Listener durdurma mesajları  
✅ **Tekrar Giriş:** Tüm veriler geri yüklenmeli  

### 11. Performans Notları

- **İlk Giriş:** 2-5 saniye sürebilir (Firebase bağlantısı)
- **Veri Ekleme:** Anında (local) + 1-2 saniye (Firebase sync)
- **Veri Çekme:** 1-3 saniye (Firebase'den indirme)
- **Offline Mod:** Desteklenir, bağlantı gelince otomatik sync

### 12. Log Kaydetme (Sorun Durumunda)

Tam log'u dosyaya kaydetmek için:
```bash
adb logcat > firebase_sync_log.txt
```

Ctrl+C ile durdurup dosyayı paylaşın.

## 🎯 Özet

Bu test talimatları ile Firebase senkronizasyonunun doğru çalıştığını doğrulayabilirsiniz. Her adımda log'ları kontrol edin ve beklenen mesajları görüp görmediğinizi kontrol edin. Herhangi bir sorunda log'ları paylaşın!
