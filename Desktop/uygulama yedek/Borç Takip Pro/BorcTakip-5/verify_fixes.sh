#!/bin/bash
# Kasa/Banka Bakiye Sorunu Doğrulama Script'i

echo "========================================="
echo "  Kasa/Banka Bakiye Düzeltme Doğrulama"
echo "========================================="
echo ""

# MainViewModel.kt kontrolü
echo "[1/3] MainViewModel.kt kontrol ediliyor..."
if grep -q 'it.category == "Kasa Girişi" || it.category == "Kasa Çıkışı"' app/src/main/java/com/burhan2855/borctakip/ui/MainViewModel.kt; then
    echo "✅ MainViewModel.kt - DÜZELTILDI"
else
    echo "❌ MainViewModel.kt - DÜZELTILMEDI (Elle düzeltme yapılmalı)"
fi
echo ""

# DebtTrackerApp.kt kontrolü
echo "[2/3] DebtTrackerApp.kt kontrol ediliyor..."
if grep -q 'it.category == "Kasa Girişi" || it.category == "Kasa Çıkışı"' app/src/main/java/com/burhan2855/borctakip/ui/DebtTrackerApp.kt; then
    echo "✅ DebtTrackerApp.kt - DÜZELTILDI"
else
    echo "❌ DebtTrackerApp.kt - DÜZELTILMEDI (Elle düzeltme yapılmalı)"
fi
echo ""

# TransactionRepository.kt - applyPartialPayment kaldırıldı mı kontrolü
echo "[3/3] TransactionRepository.kt kontrol ediliyor..."
if grep -q 'suspend fun applyPartialPayment' app/src/main/java/com/burhan2855/borctakip/data/TransactionRepository.kt; then
    echo "❌ TransactionRepository.kt - Eski applyPartialPayment hâlâ var"
else
    echo "✅ TransactionRepository.kt - Eski applyPartialPayment kaldırıldı"
fi
echo ""

echo "========================================="
echo "  Doğrulama Tamamlandı"
echo "========================================="
