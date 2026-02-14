package com.burhan2855.personeltakip.logic

import com.burhan2855.personeltakip.data.Adjustment
import com.burhan2855.personeltakip.data.AdjustmentType
import com.burhan2855.personeltakip.data.Employee
import com.burhan2855.personeltakip.data.WorkLog
import java.time.Duration
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class SalaryReport(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalHoursWorked: Double,
    val normalHours: Double,
    val overtimeHours: Double,
    val missingHours: Double,
    val totalNormalEarnings: Double,
    val totalOvertimeEarnings: Double,
    val totalMissingDeduction: Double,
    val totalAdjustments: AdjustmentSummary,
    val totalEarnings: Double,
    val totalPayments: Double,
    val remainingBalance: Double,
    val paidLeaveDays: Int,
    val unpaidLeaveDays: Int,
    val sickLeaveDays: Int,
    val annualLeaveDays: Int,
    val totalWorkingDays: Int,
    val totalMealAllowance: Double = 0.0,
    val totalTransportAllowance: Double = 0.0,
    val mealCount: Int = 0,
    val transportCount: Int = 0,
    val totalYemekCount: Int = 0,
    val totalYolCount: Int = 0,
    val remainingAnnualLeave: Int = 0,
    val unpaidLeaveDeduction: Double = 0.0,
    val sickLeaveDeduction: Double = 0.0,
    val missingHoursDeduction: Double = 0.0
) {
    val month: Int get() = startDate.monthValue
    val year: Int get() = startDate.year
}

data class IndemnityReport(
    val employee: Employee,
    val totalDaysWorked: Int,
    val years: Int,
    val months: Int,
    val remainingDays: Int,
    val dailyNetMaas: Double,
    val dailyNetYemek: Double,
    val dailyNetYol: Double,
    val totalDaily: Double,
    val kidemTazminati: Double,
    val remainingLeaveDays: Int,
    val remainingLeaveAmount: Double,
    val totalYemek: Double,
    val totalYol: Double,
    val totalPrim: Double,
    val totalAvans: Double,
    val totalKesinti: Double,
    val netTazminat: Double,
    val paidTazminat: Double,
    val remainingPayment: Double
)

data class AdjustmentSummary(
    val avansAmount: Double = 0.0,
    val avansCount: Int = 0,
    val yemekAmount: Double = 0.0,
    val yemekCount: Int = 0,
    val yolAmount: Double = 0.0,
    val yolCount: Int = 0,
    val primAmount: Double = 0.0,
    val primCount: Int = 0,
    val kesintiAmount: Double = 0.0,
    val kesintiCount: Int = 0,
    val maasOdemeAmount: Double = 0.0,
    val tazminatAmount: Double = 0.0,
    val bankPaymentAmount: Double = 0.0,
    val cashPaymentAmount: Double = 0.0,
    val yemekAdjustmentCount: Int = 0,
    val yolAdjustmentCount: Int = 0
) {
    val earningsTotal: Double get() = yemekAmount + yolAmount + primAmount
    val deductionsTotal: Double get() = avansAmount + kesintiAmount
    val paymentsTotal: Double get() = maasOdemeAmount + bankPaymentAmount + cashPaymentAmount + tazminatAmount
    val totalPaymentAmount: Double get() = paymentsTotal
}

class SalaryCalculator {
    fun calculateMonthlySalary(
        employee: Employee,
        logs: List<WorkLog>,
        adjustments: List<Adjustment>,
        year: Int,
        month: Int
    ): SalaryReport {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.plusMonths(1).minusDays(1)
        return calculateRangeSalary(employee, logs, adjustments, startDate, endDate)
    }

    fun calculateRangeSalary(
        employee: Employee,
        logs: List<WorkLog>,
        adjustments: List<Adjustment>,
        startDate: LocalDate,
        endDate: LocalDate
    ): SalaryReport {
        var totalHours = 0.0; var totalNormalHours = 0.0; var totalOvertimeHours = 0.0; var totalMissingHours = 0.0; var totalOvertimeEarnings = 0.0
        var paidLeaveDays = 0; var unpaidLeaveDays = 0; var sickLeaveDays = 0; var annualLeaveDays = 0; var workingDays = 0
        var totalMealAllowanceFromLogs = 0.0; var totalTransportAllowanceFromLogs = 0.0; var mealCountFromLogs = 0; var transportCountFromLogs = 0
        var unpaidLeaveDeduction = 0.0; var sickLeaveDeduction = 0.0; var missingHoursDeduction = 0.0
        
        val monthlyBaseline = (employee.hourlyRate * employee.dailyWorkingHours * 30) + employee.additionalSalary
        val totalDaysInMonth = (ChronoUnit.DAYS.between(startDate, endDate) + 1).toDouble()
        val dailyValue = monthlyBaseline / totalDaysInMonth
        val overtimeHourlyRate = if (employee.dailyWorkingHours > 0) monthlyBaseline / (totalDaysInMonth * employee.dailyWorkingHours) else 0.0
        
        var effectiveMissingEquivDays = 0.0
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            val dayLogs = logs.filter { it.date == currentDate }
            val isBeforeStart = employee.startDate != null && currentDate.isBefore(employee.startDate)
            val isAfterEnd = employee.endDate != null && currentDate.isAfter(employee.endDate)
            
            if (isBeforeStart || isAfterEnd) {
                effectiveMissingEquivDays += 1.0
                currentDate = currentDate.plusDays(1); continue
            }

            val hasSick = dayLogs.any { it.leaveType?.contains("Rapor", true) == true }
            if (hasSick) {
                sickLeaveDays++; effectiveMissingEquivDays += 1.0; sickLeaveDeduction += dailyValue
                currentDate = currentDate.plusDays(1); continue
            }
            
            val hasUnpaid = dayLogs.any { it.leaveType?.contains("Ücretsiz", true) == true }
            if (hasUnpaid) {
                unpaidLeaveDays++; effectiveMissingEquivDays += 1.0; unpaidLeaveDeduction += dailyValue
                currentDate = currentDate.plusDays(1); continue
            }
            
            val hasLeave = dayLogs.any { it.isOnLeave }
            if (hasLeave) {
                val leaveLog = dayLogs.first { it.isOnLeave }
                if (leaveLog.leaveType?.contains("Ücretli", true) == true) paidLeaveDays++
                if (leaveLog.leaveType?.contains("Yıllık", true) == true) annualLeaveDays++
                totalNormalHours += employee.dailyWorkingHours
                currentDate = currentDate.plusDays(1); continue
            }
            
            val workLog = dayLogs.find { it.startTime != null && it.endTime != null }
            if (workLog != null) {
                workingDays++
                val hoursWorked = Duration.between(workLog.startTime, workLog.endTime).toMinutes().toDouble() / 60.0 - (workLog.breakDurationMinutes / 60.0)
                totalHours += hoursWorked
                if (hoursWorked >= employee.dailyWorkingHours) {
                    totalNormalHours += employee.dailyWorkingHours
                    val ot = hoursWorked - employee.dailyWorkingHours
                    totalOvertimeHours += ot
                    totalOvertimeEarnings += ot * overtimeHourlyRate * employee.overtimeMultiplier
                } else {
                    totalNormalHours += hoursWorked
                    val missing = employee.dailyWorkingHours - hoursWorked
                    totalMissingHours += missing
                    val ratio = missing / employee.dailyWorkingHours
                    effectiveMissingEquivDays += ratio
                    missingHoursDeduction += ratio * dailyValue
                }
                if (workLog.hasMeal) { totalMealAllowanceFromLogs += employee.mealAllowance; mealCountFromLogs++ }
                if (workLog.hasTransport) { totalTransportAllowanceFromLogs += employee.transportAllowance; transportCountFromLogs++ }
            } else {
                totalNormalHours += employee.dailyWorkingHours; workingDays++
            }
            currentDate = currentDate.plusDays(1)
        }

        val adjSummary = calculateAdjustmentSummary(adjustments.filter { !it.date.isBefore(startDate) && !it.date.isAfter(endDate) })
        val baseDaysForCalc = totalDaysInMonth
        val earnedMonthlyBaseline = (baseDaysForCalc - effectiveMissingEquivDays).coerceAtLeast(0.0) * dailyValue
        val totalEarnings = earnedMonthlyBaseline + totalOvertimeEarnings + adjSummary.earningsTotal - adjSummary.deductionsTotal + totalMealAllowanceFromLogs + totalTransportAllowanceFromLogs

        return SalaryReport(
            startDate, endDate, totalHours, totalNormalHours, totalOvertimeHours, totalMissingHours, earnedMonthlyBaseline, totalOvertimeEarnings,
            ((baseDaysForCalc * dailyValue) - earnedMonthlyBaseline).coerceAtLeast(0.0), adjSummary, totalEarnings, adjSummary.paymentsTotal,
            totalEarnings - adjSummary.paymentsTotal, paidLeaveDays, unpaidLeaveDays, sickLeaveDays, annualLeaveDays, workingDays,
            totalMealAllowanceFromLogs, totalTransportAllowanceFromLogs, mealCountFromLogs, transportCountFromLogs,
            mealCountFromLogs + adjSummary.yemekAdjustmentCount, transportCountFromLogs + adjSummary.yolAdjustmentCount,
            employee.annualLeaveEntitlement - annualLeaveDays, unpaidLeaveDeduction, sickLeaveDeduction, missingHoursDeduction
        )
    }

    fun calculateIndemnity(employee: Employee, allLogs: List<WorkLog>, allAdjustments: List<Adjustment>): IndemnityReport? {
        val start = employee.startDate ?: return null
        val end = employee.endDate ?: return null
        if (end.isBefore(start)) return null

        // ACTUAL days between dates (inclusive)
        val totalDaysWorked = (ChronoUnit.DAYS.between(start, end) + 1).toInt()
        
        // Components (Years, Months, Days) calculation
        var y = 0; var m = 0; var d = 0
        var temp = start
        while (!temp.plusYears(1).isAfter(end)) { y++; temp = temp.plusYears(1) }
        while (!temp.plusMonths(1).isAfter(end)) { m++; temp = temp.plusMonths(1) }
        d = (ChronoUnit.DAYS.between(temp, end) + 1).toInt()

        // Monthly salary defined as fixed sum (or 30 * hourly)
        val monthlySalary = (employee.hourlyRate * 30 * employee.dailyWorkingHours) + employee.additionalSalary
        
        // Yeni formül: maaş / 365 * toplam gün
        val kidem = (monthlySalary / 365.0) * totalDaysWorked

        val dailyMaas = monthlySalary / 30.0
        val adjSum = calculateAdjustmentSummary(allAdjustments)
        val logsSum = allLogs.filter { !it.date.isBefore(start) && !it.date.isAfter(end) }.let { periodLogs ->
            var yAmt = 0.0; var rAmt = 0.0
            periodLogs.forEach { if (it.hasMeal) yAmt += employee.mealAllowance; if (it.hasTransport) rAmt += employee.transportAllowance }
            yAmt to rAmt
        }
        val totalYemek = logsSum.first + adjSum.yemekAmount; val totalYol = logsSum.second + adjSum.yolAmount
        val leaveDays = allLogs.filter { it.isOnLeave && it.leaveType?.contains("Yıllık", true) == true }.size
        val leaveAmt = (employee.annualLeaveEntitlement - leaveDays) * dailyMaas

        // Tazminat net hesabı: Kıdem + İzin + Prim - Avans - Kesinti (Puantajdan gelen yemek/yol hariç)
        val net = kidem + leaveAmt + adjSum.primAmount - adjSum.avansAmount - adjSum.kesintiAmount
        
        return IndemnityReport(
            employee, totalDaysWorked, y, m, d, dailyMaas, employee.mealAllowance, employee.transportAllowance, dailyMaas + employee.mealAllowance + employee.transportAllowance,
            kidem, employee.annualLeaveEntitlement - leaveDays, leaveAmt, totalYemek, totalYol, adjSum.primAmount, adjSum.avansAmount, adjSum.kesintiAmount,
            net, adjSum.tazminatAmount, net - adjSum.tazminatAmount
        )
    }

    private fun calculateAdjustmentSummary(adjustments: List<Adjustment>): AdjustmentSummary {
        var avansAmt = 0.0; var avansCnt = 0; var yemekAmt = 0.0; var yemekCnt = 0; var yolAmt = 0.0; var yolCnt = 0
        var primAmt = 0.0; var primCnt = 0; var kesintiAmt = 0.0; var kesintiCnt = 0
        var maasAmt = 0.0; var tazminatAmt = 0.0; var bankAmt = 0.0; var cashAmt = 0.0
        adjustments.forEach { adj ->
            when (adj.type) {
                AdjustmentType.AVANS -> { avansAmt += adj.amount; avansCnt++ }
                AdjustmentType.YEMEK -> { yemekAmt += adj.amount; yemekCnt++ }
                AdjustmentType.YOL -> { yolAmt += adj.amount; yolCnt++ }
                AdjustmentType.PRIM -> { primAmt += adj.amount; primCnt++ }
                AdjustmentType.KESINTI -> { kesintiAmt += adj.amount; kesintiCnt++ }
                AdjustmentType.MAAS_ODEME -> maasAmt += adj.amount
                AdjustmentType.TAZMINAT_ODEME -> tazminatAmt += adj.amount
                AdjustmentType.BANKA_ODEME -> bankAmt += adj.amount
                AdjustmentType.ELDEN_ODEME -> cashAmt += adj.amount
            }
        }
        return AdjustmentSummary(avansAmt, avansCnt, yemekAmt, yemekCnt, yolAmt, yolCnt, primAmt, primCnt, kesintiAmt, kesintiCnt, maasAmt, tazminatAmt, bankAmt, cashAmt, yemekCnt, yolCnt)
    }
}
