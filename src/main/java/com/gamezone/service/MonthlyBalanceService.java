package com.gamezone.service;

import com.gamezone.model.Return;
import com.gamezone.model.Sale;
import java.io.IOException;

/**
 * Computes the monthly financial balance of the store: the total of the
 * sales, the total refunded in returns, and the net balance between them.
 * Reporting is kept apart from {@link ReturnService}, which only processes
 * and consults returns (SOLID audit finding V-16).
 */
public class MonthlyBalanceService {

    private final SaleService saleService;
    private final ReturnService returnService;

    /**
     * Creates the service with the services that provide the sales and the returns.
     *
     * @param saleService   the service used to list the sales
     * @param returnService the service used to list the returns
     */
    public MonthlyBalanceService(SaleService saleService, ReturnService returnService) {
        this.saleService = saleService;
        this.returnService = returnService;
    }

    /**
     * Calculates the total of the sales made in a given month and year.
     *
     * @param month the month (1-12)
     * @param year  the year
     * @return the sum of the final total of every sale of that period
     * @throws IOException if the sales cannot be read from storage
     */
    public double calculateMonthlySales(int month, int year) throws IOException {
        double totalSales = 0.0;
        for (Sale sale : saleService.listSales()) {
            if (sale.getDate().getMonthValue() == month && sale.getDate().getYear() == year) {
                totalSales += sale.getFinalTotal();
            }
        }
        return totalSales;
    }

    /**
     * Calculates the total refunded in the returns made in a given month and year.
     *
     * @param month the month (1-12)
     * @param year  the year
     * @return the sum of the refund amount of every return of that period
     * @throws IOException if the returns cannot be read from storage
     */
    public double calculateMonthlyReturns(int month, int year) throws IOException {
        double totalReturns = 0.0;
        for (Return r : returnService.viewAllReturns()) {
            if (r.getDate().getMonthValue() == month && r.getDate().getYear() == year) {
                totalReturns += r.getRefundAmount();
            }
        }
        return totalReturns;
    }

    /**
     * Calculates the net balance (sales minus returns) for a given month and year.
     *
     * @param month the month (1-12)
     * @param year  the year
     * @return the total sales minus the total returns for that period
     * @throws IOException if sales or returns cannot be read from storage
     */
    public double generateMonthlyBalance(int month, int year) throws IOException {
        return calculateMonthlySales(month, year) - calculateMonthlyReturns(month, year);
    }
}