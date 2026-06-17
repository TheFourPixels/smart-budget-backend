package com.teamfourpixels.generator;

import com.teamfourpixels.dto.CategoryDto;
import com.teamfourpixels.dto.TransactionDto;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class BankDataGenerator {

    private static final List<TransactionTemplate> TRANSACTION_TEMPLATES = List.of(
            new TransactionTemplate(new BigDecimal("-4500.25"), "Супермаркет 'Лента'", "5411",
                    "Еженедельная закупка продуктов", 1L, "Продукты (Банк)"),
            new TransactionTemplate(new BigDecimal("95000.00"), "Работодатель ООО", null,
                    "Зарплата за текущий месяц", 2L, "Зачисления (Банк)"),
            new TransactionTemplate(new BigDecimal("-1250.00"), "Ресторан 'Уют'", "5812",
                    "Ужин с друзьями", 3L, "Общепит (Банк)"),
            new TransactionTemplate(new BigDecimal("-3200.00"), "МосЭнергоСбыт", "4900",
                    "Оплата электроэнергии", 4L, "ЖКХ (Банк)"),
            new TransactionTemplate(new BigDecimal("-2100.00"), "АЗС Лукойл", "5542",
                    "Бензин АИ-95", 5L, "Топливо (Банк)"),
            new TransactionTemplate(new BigDecimal("-7999.00"), "Zara Store", "5651",
                    "Покупка рубашки", 6L, "Одежда (Банк)"),
            new TransactionTemplate(new BigDecimal("-5000.00"), "Перевод Другу", null,
                    "Перевод долга", 7L, "Переводы (Банк)")
    );

    public List<TransactionDto> fetchTransactions(int year, int month) {
        ZonedDateTime startOfMonth = ZonedDateTime.of(year, month, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        Instant baseDate = startOfMonth.toInstant();
        long maxDays = 28;

        return IntStream.range(0, TRANSACTION_TEMPLATES.size())
                .mapToObj(i -> generateMockTransaction(i, baseDate, maxDays, TRANSACTION_TEMPLATES.get(i)))
                .toList();
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
                .amount(template.amount())
                .externalId("bank-ref-" + baseDate.getEpochSecond() + "-" + index)
                .transactionDate(transactionDate)
                .description(template.description())
                .merchantName(template.merchantName())
                .mcc(template.mcc())
                .category(CategoryDto.builder()
                        .id(template.bankCategoryId())
                        .name(template.bankCategoryName())
                        .isSystem(false)
                        .build())
                .build();
    }
}