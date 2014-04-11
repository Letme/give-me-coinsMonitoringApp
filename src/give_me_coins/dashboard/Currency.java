package give_me_coins.dashboard;

import android.content.res.Resources;

enum Currency {
    LTC(R.color.ltc),
    BTC(R.color.btc),
    FTC(R.color.ftc),
    VTC(R.color.vtc),
    PPC(R.color.ppc);

    private final int color;

    Currency(int color) {
        this.color = color;
    }

    int color(Resources resources) {
        return resources.getColor(color);
    }
}
