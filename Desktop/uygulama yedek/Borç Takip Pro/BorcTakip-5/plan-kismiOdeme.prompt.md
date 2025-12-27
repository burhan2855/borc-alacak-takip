KONU: Kısmi ödeme yapıldığında borç bakiyesinin güncellenmemesi — Hata ayıklama ve düzeltme planı

Amaç
- "İşlem" menüsünden yapılan kısmi ödemenin borç (Debt) bakiyesinden doğru şekilde düşmesini sağlamak ve UI'nın anlık olarak güncellenmesini garantilemek.

Varsayımlar
- Proje Android + Kotlin (Jetpack Compose), Room kullanıyor.
- Kısmi ödeme işleyişi UI -> ViewModel -> Repository -> DAO zinciriyle ilerliyor.
- Kullanıcı daha önce bazı dosyaları paylaşmayı planladı ama henüz göndermedi.
- Dosya ve paket isimleri repo konvansiyonuna göre değişebilir; gerekli dosyalar araştırılacak.

Kısa checklist (eksiksiz, adım adım)
1. Repro: Kısmi ödeme işlemini local olarak tekrar üret (manüel adımlar, expected vs actual kayıt et).
2. Kodu incele: aşağıdaki dosyaları aç ve kritik fonksiyonları bul:
   - `TransactionMenu` veya kısmi ödeme UI handler
   - `DebtViewModel` (veya ilgili ViewModel)
   - `DebtRepository` (veya Repo muhtemel adıyla)
   - `DebtDao` (Room DAO)
   - `Debt` entity
3. İşlem mantığını kontrol et:
   - Ödeme miktarı hesaplandıktan sonra `debt.balance -= amount` veya eşdeğeri uygulanıyor mu?
   - Değişiklik DAO ile `@Update` veya `@Query("UPDATE ...")` kullanılarak veritabanına yazılıyor mu?
   - DAO çağrısı `suspend` mi? IO thread'e dispatch ediliyor mu? (Dispatchers.IO / viewModelScope.launch(Dispatchers.IO))
4. Transactional tutarlılık:
   - Eğer birden fazla tablo/güncelleme varsa Room `@Transaction` veya DB transaction kullan.
5. UI state yayılımı:
   - ViewModel değişiklikleri Flow/StateFlow/LiveData ile emit ediyor mu?
   - Compose tarafı bu Flow/StateFlow‘i `collectAsState()` ile dinliyor mu?
   - Eğer UI sadece bir değer kopyası gösteriyorsa (`remember { mutableStateOf(...) }`), koleksiyon yapısı güncellenmiyor olabilir.
6. Hata/Exception kontrolü:
   - Logcat'te (adb logcat) SQLException, KSP veya Room uyarıları var mı?
   - Coroutine hata yakalayıcıları (try/catch) hatayı yutuyor mu?
7. Basit düzeltme seçenekleri (öneriler):
   - DAO'da doğrudan sorgu kullan: `@Query("UPDATE debts SET balance = balance - :amount WHERE id = :id") suspend fun applyPartialPayment(id: Long, amount: Double)` — atomik ve hızlı.
   - ViewModel'de `viewModelScope.launch { repository.applyPartialPayment(id, amount) }` ve repository IO dispatch ile çağrılsın.
   - ViewModel state'ini güncelle: `stateFlow` kullanıyorsan, işlemden sonra gerekirse `refreshDebt(id)` ile en güncel veriyi çek.
8. Testler ve doğrulama:
   - Birim testi: DAO için bir test yaz (in-memory Room) kısmi ödeme sorgusunun beklendiği sonucu verdiğini doğrula.
   - Instrumented veya manual smoke test: uygulamada kısmi ödeme yap, detay ekranındaki bakiye ve ana liste/bakiye aynı olmalı.
9. KSP/Room yeniden oluşturma adımları (gerekirse):
   - `./gradlew :app:clean :app:assembleDebug` (Windows PowerShell için: `.\gradlew.bat :app:clean :app:assembleDebug`) — KSP artefaktları güncellenmeli.
10. PR notları ve migration notları (eğer entity değişikliği varsa):
   - DB versiyon artışı, migration SQL veya Room migration nesnesi ekle.
   - Yapılan Gradle / KSP temizliği ve test komutlarını PR açıklamasına ekle.

Detaylı adımlar (uygulama sırasında yapılacaklar)
A. Repro & Log
 - Adım 1: Uygulamayı debug modda aç.
 - Adım 2: İşlem menüsünden kısmi ödeme gerçekleştir (ör. borç 1000 TL, kısmi ödeme 200 TL).
 - Beklenti: bakiye 800 TL olmalı. Gerçek durum: bakiye değişmiyor.
 - Adım 3: Logcat filtresi `com.your.package.name` ile hataları incele.

B. Kodu takip et
 - UI -> hangi fonksiyon tetikleniyor? (buton onClick handler)
 - Handler içinde gönderilen amount değeri doğru mu? (println/log)
 - Handler, ViewModel üzerinden repository'e çağrı yapıyorsa, ViewModel'in ilgili fonksiyonu açılacak ve takip edilecek.

C. Repository / DAO incele
 - DAO: `@Update` kullanılıyorsa, gönderilen nesne gerçekten primary key ile eşleşiyor mu?
 - `@Update` sadece nesne alanlarını baz alır; eğer aynı nesne kopyası değilse veya primary key yanlışsa güncelleme olmayabilir.
 - Daha güvenli: atomik `@Query("UPDATE debts SET balance = :newBalance WHERE id = :id")` veya `... = balance - :amount ...` kullanın.

D. UI State
 - Eğer veriler Compose içinde `remember` ile tutuluyor ve DB Flow ile bağlı değilse, update sonrası UI yenilenmiyor olabilir.
 - Tercih: Repository DB'den Flow döndürsün; ViewModel bu Flow'u state'e dönüştürsün (örnek: `val debt = repository.getDebtFlow(id).stateIn(...)`) ve Compose `collectAsState()` ile dinlesin.

E. Atomiklık & concurrency
 - Aynı anda paralel işlemler (background sync, remote sync) balance'ı geri döndürebilir. Ödeme uygulandıktan sonra en güncel veriyi force çekmek veya optimistic update yapıp sonradan reconcile etmek gerekebilir.

F. Hızlı düzeltme patch önerisi
 - DAO'ya yeni fonksiyon ekle:
   `@Query("UPDATE debts SET balance = balance - :amount WHERE id = :id")
    suspend fun applyPartialPayment(id: Long, amount: Double)`
 - Repository bunu çağıran basit wrapper yazsın.
 - ViewModel `viewModelScope.launch(Dispatchers.IO)` içinde çağırıp işlem başarılıysa UI state'i refresh etsin.

G. Test senaryoları
 - DAO in-memory testi: başlangıç 100.0, applyPartialPayment(id, 20.0) sonra balance==80.0
 - ViewModel integration testi (kısa): fake repo ile çağrı yapıldığında state güncelleniyor mu?

H. PR açıklaması ve QA notları
 - Yapılan değişiklikler, hangi dosyalar değişti, hangi komutlar çalıştırıldı (clean KSP vs build), nasıl test edildi.
 - Database migration gerekliyse migration SQL eklenip test edilecek.

Beklenen çıktı (başarı kriteri)
- Kısmi ödeme uygulandığında veritabanındaki `balance` değeri doğru olarak azalır.
- UI anında (veya kısa süre içinde) yeni bakiye gösterilir.
- Hiçbir istisna veya KSP/Room uyarısı oluşmaz.

Ek notlar
- Eğer repo içinde uzak senkronizasyon (Firebase/Firestore) varsa, ödeme hem local DB'ye hem de uzak servise tutarlı şekilde gönderilmeli; local öncelikli, sonra remote. Çakışma yönetimi tasarlanmalı.
- Eğer kullanıcı hatalı miktar girerse (negative, zero, büyük miktar) guard clause'lar ekle.

Sonraki adımlar (önerilen sıra)
1. Repro yap, logları al.  
2. İlgili dosyaları paylaş veya hemen repo içinde arama yapıp `DebtDao`/`DebtViewModel` dosyalarını incele.  
3. Hızlı patch uygula (DAO `applyPartialPayment` ile) ve local test et.  
4. Birim testi ekle, KSP temizle ve derle, sonra PR hazırla.

