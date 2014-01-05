package give_me_coins.dashboard;

import android.app.Application;

public class GiveMeCoinsDashboard extends Application {
    static GiveMeCoinsDashboard instance;

    private static Currency currency = Currency.LTC;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    static GiveMeCoinsDashboard instance() {
        return instance;
    }

    void set(Currency selectedCurrency) {
       currency = selectedCurrency;
    }

    static Currency selectedCurrency() {
        return currency;
    }
}
