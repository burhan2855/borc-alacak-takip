# Firebase Yedekleme Sorununun Çözümü

## Sorun
Google hesabıyla giriş yapılmasına rağmen veriler Firebase'e yedeklenmiyordu. Çıkış yapıp tekrar giriş yapıldığında veriler kaybolmuş gibi görünüyordu.

## Kök Neden
Firebase senkronizasyonu hiçbir zaman başlatılmıyordu. `TransactionRepository.startListeningForChanges()` ve `ContactRepository.startListeningForChanges()` fonksiyonları tanımlanmış ama hiçbir yerde çağrılmıyordu.

## Yapılan Değişiklikler

### 1. DebtApplication.kt
- `onCreate()` metodu eklendi
- Firebase Auth durumu dinleniyor
- Kullanıcı giriş yaptığında:
  - `mainViewModel.initializeDataSync()` çağrılıyor (TransactionRepository senkronizasyonu)
  - `contactRepository.startListeningForChanges()` çağrılıyor (ContactRepository senkronizasyonu)
- Kullanıcı çıkış yaptığında:
  - Her iki repository için de `stopListeningForChanges()` çağrılıyor

### 2. ContactRepository.kt (Tamamen yeniden yazıldı)
- Firebase Firestore entegrasyonu eklendi
- `startListeningForChanges()`: Firebase'den real-time güncellemeleri dinler
- `stopListeningForChanges()`: Listener'ı durdurur
- `insert()`: Yeni contact hem Room'a hem Firebase'e kaydedilir
- `clearAllLocalData()`: Çıkış yapıldığında yerel verileri temizler

### 3. Contact.kt
- `documentId: String = ""` alanı eklendi (Firebase document ID'si için)
- `@get:Exclude` annotation ile Firebase serialization'dan hariç tutuldu

### 4. ContactDao.kt
- `updateContact()`: Contact güncelleme fonksiyonu
- `deleteAllContacts()`: Tüm contact'ları silme
- `insertAll()`: Toplu contact ekleme
- `syncContacts()`: Extension function - Firebase'den gelen verilerle yerel DB'yi senkronize eder

### 5. MainViewModel.kt
- `onSignOut()` fonksiyonuna ContactRepository temizleme komutları eklendi
- Çıkış yapılınca hem transaction hem contact verileri temizlenir

## Nasıl Çalışır?

### Giriş Yapıldığında:
1. `DebtApplication.onCreate()` içinde Firebase Auth listener aktif
2. Kullanıcı giriş yapar
3. Auth listener tetiklenir ve senkronizasyon başlar
4. `TransactionRepository.startListeningForChanges()` çağrılır
5. `ContactRepository.startListeningForChanges()` çağrılır
6. Firebase'deki mevcut veriler otomatik olarak yerel Room DB'ye indirilir

### Yeni Veri Eklendiğinde:
1. Kullanıcı yeni bir transaction/contact ekler
2. Önce Room DB'ye kaydedilir
3. Sonra Firebase Firestore'a senkronize edilir
4. Document ID geri alınır ve Room'da güncellenir

### Çıkış Yapıldığında:
1. `mainViewModel.onSignOut()` çağrılır
2. Her iki repository için listener durdurulur
3. Yerel veriler temizlenir (güvenlik için)
4. Firebase Auth'dan çıkış yapılır

### Tekrar Giriş Yapıldığında:
1. Firebase Auth listener tekrar tetiklenir
2. Senkronizasyon yeniden başlar
3. Firebase'deki tüm veriler yerel DB'ye geri yüklenir
4. Kullanıcı verilerini geri görür

## Test Etmek İçin

1. Uygulamayı derleyin:
```powershell
.\gradlew.bat :app:assembleDebug
```

2. APK'yı yükleyin ve çalıştırın

3. Google hesabınızla giriş yapın

4. Logcat'te şu mesajları görmelisiniz:
   - `DebtApplication: User signed in: [email], starting Firebase sync`
   - `DB_DUMP: Firestore listener registered`
   - `ContactRepo: Firestore listener registered for contacts`

5. Birkaç borç/alacak ve contact ekleyin

6. Logcat'te Firebase senkronizasyonu göreceksiniz:
   - `Repository.insert: Firestore document created: [document_id]`
   - `ContactRepo: Firestore document created: [document_id]`

7. Çıkış yapın

8. Tekrar giriş yapın - verileriniz geri yüklenmiş olmalı!

## Firebase Console'dan Kontrol

1. Firebase Console'a gidin: https://console.firebase.google.com
2. Projenizi seçin: BorcTakip (veya proje adınız)
3. Firestore Database'e gidin
4. `users / [user_id] / transactions` koleksiyonunu kontrol edin
5. `users / [user_id] / contacts` koleksiyonunu kontrol edin
6. Eklediğiniz verileri görmelisiniz

## Önemli Notlar

- Firebase senkronizasyonu internet bağlantısı gerektirir
- İlk giriş sonrası veriler Firebase'e yüklenmeye başlar
- Çıkış yapınca yerel veriler güvenlik için silinir
- Tekrar giriş yapınca Firebase'den otomatik geri yüklenir
- Offline çalışma desteklenir (Firebase Offline Persistence sayesinde)

## Sorun Devam Ederse

Logcat'i kontrol edin:
```powershell
adb logcat -s DebtApplication:D DB_DUMP:D ContactRepo:D
```

Şu hataları arayın:
- Firebase bağlantı hataları
- Auth hataları
- Firestore permission hataları
