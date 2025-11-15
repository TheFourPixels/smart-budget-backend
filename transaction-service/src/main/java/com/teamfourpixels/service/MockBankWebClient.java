package com.teamfourpixels.service;

import com.teamfourpixels.dto.CategoryDto;
import com.teamfourpixels.dto.TransactionDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class MockBankWebClient {

    private static class TransactionTemplate {
        final BigDecimal amount;
        final String merchantName;
        final String mcc;
        final String description;
        final Long bankCategoryId;
        final String bankCategoryName;

        TransactionTemplate(BigDecimal amount, String merchantName, String mcc, String description, Long bankCategoryId, String bankCategoryName) {
            this.amount = amount;
            this.merchantName = merchantName;
            this.mcc = mcc;
            this.description = description;
            this.bankCategoryId = bankCategoryId;
            this.bankCategoryName = bankCategoryName;
        }
    }

    private static final List<TransactionTemplate> TRANSACTION_TEMPLATES = List.of(
            // 1. Расходы: Продукты (MCC 5411)
            new TransactionTemplate(new BigDecimal("-4500.25"), "Супермаркет 'Лента'", "5411", "Еженедельная закупка продуктов",
                    1L, "Продукты (Банк)"),
            // 2. Доходы: Зарплата (Нет MCC)
            new TransactionTemplate(new BigDecimal("95000.00"), "Работодатель ООО", null, "Зарплата за текущий месяц",
                    1L, "Зачисления (Банк)"),
            // 3. Расходы: Кафе/Рестораны (MCC 5812)
            new TransactionTemplate(new BigDecimal("-1250.00"), "Ресторан 'Уют'", "5812", "Ужин с друзьями",
                    1L, "Общепит (Банк)"),
            // 4. Расходы: Коммунальные услуги (MCC 4900)
            new TransactionTemplate(new BigDecimal("-3200.00"), "МосЭнергоСбыт", "4900", "Оплата электроэнергии",
                    1L, "ЖКХ (Банк)"),
            // 5. Расходы: Топливо (MCC 5541)
            new TransactionTemplate(new BigDecimal("-2100.00"), "АЗС Лукойл", "5541", "Бензин АИ-95",
                    1L, "Топливо (Банк)"),
            // 6. Расходы: Одежда (MCC 5651)
            new TransactionTemplate(new BigDecimal("-7999.00"), "Zara Store", "5651", "Покупка рубашки",
                    1L, "Одежда (Банк)"),
            // 7. Расходы: Перевод (Нет MCC)
            new TransactionTemplate(new BigDecimal("-5000.00"), "Перевод Другу", null, "Перевод долга",
                    1L, "Переводы (Банк)")
    );

    public List<TransactionDto> fetchTransactions(int year, int month) {

        ZonedDateTime startOfMonth = ZonedDateTime.of(year, month, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        Instant baseDate = startOfMonth.toInstant();

        long maxDays = 28;

        return IntStream.range(0, TRANSACTION_TEMPLATES.size())
                .mapToObj(i -> generateMockTransaction(i, baseDate, maxDays, TRANSACTION_TEMPLATES.get(i)))
                .collect(Collectors.toList());
    }

    private TransactionDto generateMockTransaction(int index, Instant baseDate, long maxDays, TransactionTemplate template) {
        long randomDay = (long) (Math.random() * maxDays) + 1;
        long randomHour = (long) (Math.random() * 24);
        long randomMinute = (long) (Math.random() * 60);

        Instant transactionDate = baseDate
                .plus(randomDay, ChronoUnit.DAYS)
                .plus(randomHour, ChronoUnit.HOURS)
                .plus(randomMinute, ChronoUnit.MINUTES);

        return TransactionDto.builder()
                .id(null)
                .amount(template.amount)
                .external_id("bank-ref-" + baseDate.getEpochSecond() + "-" + index)
                .transaction_date(transactionDate)
                .description(template.description)
                .merchant_name(template.merchantName)
                .mcc(template.mcc)
                .category(CategoryDto.builder()
                        .id(template.bankCategoryId)
                        .name(template.bankCategoryName)
                        .build())
                .parent_transaction_id(null)
                .build();
    }
}