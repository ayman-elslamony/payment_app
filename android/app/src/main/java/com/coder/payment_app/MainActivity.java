package com.coder.payment_app; // must be identical with this

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.oppwa.mobile.connect.checkout.dialog.CheckoutActivity;
import com.oppwa.mobile.connect.checkout.meta.CheckoutSettings;
import com.oppwa.mobile.connect.checkout.meta.CheckoutStorePaymentDetailsMode;
import com.oppwa.mobile.connect.exception.PaymentError;
import com.oppwa.mobile.connect.exception.PaymentException;
import com.oppwa.mobile.connect.payment.BrandsValidation;
import com.oppwa.mobile.connect.payment.CheckoutInfo;
import com.oppwa.mobile.connect.payment.ImagesRequest;
import com.oppwa.mobile.connect.payment.PaymentParams;
import com.oppwa.mobile.connect.payment.card.CardPaymentParams;
import com.oppwa.mobile.connect.payment.stcpay.STCPayPaymentParams;
import com.oppwa.mobile.connect.payment.stcpay.STCPayVerificationOption;
import com.oppwa.mobile.connect.payment.token.TokenPaymentParams;
import com.oppwa.mobile.connect.provider.Connect;
import com.oppwa.mobile.connect.provider.ITransactionListener;
import com.oppwa.mobile.connect.provider.OppPaymentProvider;
import com.oppwa.mobile.connect.provider.Transaction;
import com.oppwa.mobile.connect.provider.TransactionType;

import java.util.LinkedHashSet;
import java.util.Set;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;


public class MainActivity extends FlutterActivity implements ITransactionListener,MethodChannel.Result {

    private String checkoutId = "";
    private  MethodChannel.Result Result;
    private String type = "";
    private  String mode = "";
    private String brands = "";
    private String PayTypeSotredCard = "";
    private String Lang = "";
    private String EnabledTokenization = "";
    private String ShopperResultUrl = "";
    private String setStorePaymentDetailsMode = "";
    private String number, holder, cvv, year, month;
    private String TokenID = "";
    private OppPaymentProvider paymentProvider  = null ;


    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);

        String CHANNEL = "Hyperpay.demo.fultter/channel";
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL).setMethodCallHandler(
                (call, result) -> {

                    Result = result;
                    if (call.method.equals("gethyperpayresponse")) {

                        type = call.argument("type");
                        mode = call.argument("mode");
                        checkoutId = call.argument("checkoutid");
                        Lang = call.argument("lang");
                        ShopperResultUrl = call.argument("ShopperResultUrl");
                        brands = call.argument("brand");
                        setStorePaymentDetailsMode = call.argument("setStorePaymentDetailsMode");

                        switch (type) {
                            case "ReadyUI":
                                openCheckoutUI(checkoutId);

                                break;
                            case "StoredCards":

                                cvv = call.argument("cvv");
                                TokenID = call.argument("TokenID");
                                storedCardPayment(checkoutId);
                                break;
                            case "CustomUI":

                                brands = call.argument("brand");
                                number = call.argument("card_number");
                                holder = call.argument("holder_name");
                                year = call.argument("year");
                                month = call.argument("month");
                                cvv = call.argument("cvv");
                                EnabledTokenization = call.argument("EnabledTokenization");
                                PayTypeSotredCard = call.argument("PayTypeSotredCard");
                                TokenID = call.argument("TokenID");

                                openCustomUI(checkoutId);
                                break;
                            default:
                                error("1", "THIS TYPE NO IMPLEMENT IN ANDROID", "");
                                break;
                        }

                    } else {
                        error("1","METHOD NAME IS NOT FOUND","");
                    }
                });

    }

    private void openCheckoutUI(String checkoutId) {

        Set<String> paymentBrands = new LinkedHashSet<>();

        switch (brands) {
            case "MADA":
                paymentBrands.add("MADA");
                break;
            case "STC_PAY":
                paymentBrands.add("STC_PAY");
                break;
            case "VISA":
                paymentBrands.add("VISA");
                break;
            case "MASTER":
                paymentBrands.add("MASTER");
                break;
            default:
                paymentBrands.add("MASTER");
                paymentBrands.add("VISA");
                break;
        }

        // CHECK PAYMENT MODE
        CheckoutSettings checkoutSettings;
        if (mode.equals("LIVE")) {
            //LIVE MODE
            checkoutSettings = new CheckoutSettings(checkoutId, paymentBrands,
                    Connect.ProviderMode.LIVE);
        } else {
            // TEST MODE
            checkoutSettings = new CheckoutSettings(checkoutId, paymentBrands,
                    Connect.ProviderMode.TEST);
        }

        // SET LANG
        checkoutSettings.setLocale(Lang);

        if (!brands.equals("STC_PAY")) {
            checkoutSettings.getStorePaymentDetailsMode();
        }
        //SHOW TOTAL PAYMENT AMOUNT IN BUTTON
        checkoutSettings.setTotalAmountRequired(true);

        //SET SHOPPER
        checkoutSettings.setShopperResultUrl(ShopperResultUrl + "://result");

        //SAVE PAYMENT CARDS FOR NEXT
        if (setStorePaymentDetailsMode.equals("true")) {
            checkoutSettings.setStorePaymentDetailsMode(CheckoutStorePaymentDetailsMode.PROMPT);
        }

        //CHANGE THEME
        checkoutSettings.setThemeResId(R.style.NewCheckoutTheme);

        // CheckoutBroadcastReceiver
        ComponentName componentName = new ComponentName(
                getPackageName(), CheckoutBroadcastReceiver.class.getName());

        /* Set up the Intent and start the checkout activity. */
        Intent intent = checkoutSettings.createCheckoutActivityIntent(this, componentName);

        startActivityForResult(intent, CheckoutActivity.REQUEST_CODE_CHECKOUT);

    }

    private void openCustomUI(String checkoutId) {

        if (brands.equals("STC_PAY")) {
            try {
                //Set Mode
                boolean resultMode = mode.equals("TEST");
                Connect.ProviderMode providerMode ;

                if (resultMode) {
                    providerMode =  Connect.ProviderMode.TEST ;
                } else {
                    providerMode =  Connect.ProviderMode.LIVE ;
                }

                STCPayPaymentParams stcPayPaymentParams = new STCPayPaymentParams(checkoutId, STCPayVerificationOption.MOBILE_PHONE);

                stcPayPaymentParams.setMobilePhoneNumber(number);

                stcPayPaymentParams.setShopperResultUrl(ShopperResultUrl + "://result");

                Transaction transaction = new Transaction(stcPayPaymentParams);

                paymentProvider = new OppPaymentProvider( getBaseContext() , providerMode);

                //Submit Transaction
                //Listen for transaction Completed - transaction Failed
                paymentProvider.submitTransaction(transaction, this);

            } catch (PaymentException e) {
                e.printStackTrace();
            }
        } else {

            if (PayTypeSotredCard.equals("true")) {
                storedCardPayment(checkoutId);
            }
            else {

                if (!CardPaymentParams.isNumberValid(number , true)) {
                    Toast.makeText(this, "Card number is not valid for brand", Toast.LENGTH_SHORT).show();
                } else if (!CardPaymentParams.isHolderValid(holder)) {
                    Toast.makeText(this, "Holder name is not valid", Toast.LENGTH_SHORT).show();
                } else if (!CardPaymentParams.isExpiryYearValid(year)) {
                    Toast.makeText(this, "Expiry year is not valid", Toast.LENGTH_SHORT).show();
                } else if (!CardPaymentParams.isExpiryMonthValid(month)) {
                    Toast.makeText(this, "Expiry month is not valid", Toast.LENGTH_SHORT).show();
                } else if (!CardPaymentParams.isCvvValid(cvv)) {
                    Toast.makeText(this, "CVV is not valid", Toast.LENGTH_SHORT).show();
                } else {
                    boolean EnabledTokenizationTemp = EnabledTokenization.equals("true");
                    try {
                        PaymentParams paymentParams = new CardPaymentParams(
                                checkoutId,
                                brands,
                                number,
                                holder,
                                month,
                                year,
                                cvv
                        ).setTokenizationEnabled(EnabledTokenizationTemp);//Set Enabled TokenizationTemp

                        paymentParams.setShopperResultUrl(ShopperResultUrl + "://result");

                        Transaction transaction = new Transaction(paymentParams);

                        //Set Mode;
                        boolean resultMode = mode.equals("TEST");
                        Connect.ProviderMode providerMode ;

                        if (resultMode) {
                            providerMode =  Connect.ProviderMode.TEST ;
                        } else {
                            providerMode =  Connect.ProviderMode.LIVE ;
                        }

                        paymentProvider = new OppPaymentProvider( getBaseContext() , providerMode);

                        //Submit Transaction
                        //Listen for transaction Completed - transaction Failed
                        paymentProvider.submitTransaction(transaction, this);

                    } catch (PaymentException e) {
                        error(
                                "0.1",
                                e.getLocalizedMessage(),
                                ""
                        );
                    }
                }
            }
        }
    }

    private void storedCardPayment(String checkoutId) {

        try {

            TokenPaymentParams paymentParams = new TokenPaymentParams(checkoutId, TokenID, brands, cvv);

            paymentParams.setShopperResultUrl(ShopperResultUrl + "://result");

            Transaction transaction = new Transaction(paymentParams);

            //Set Mode;
            boolean resultMode = mode.equals("TEST");
            Connect.ProviderMode providerMode ;

            if (resultMode) {
                providerMode =  Connect.ProviderMode.TEST ;
            } else {
                providerMode =  Connect.ProviderMode.LIVE ;
            }

            paymentProvider = new OppPaymentProvider( getBaseContext() , providerMode);

            //Submit Transaction
            //Listen for transaction Completed - transaction Failed
            paymentProvider.submitTransaction(transaction, this);

        } catch (PaymentException e) {
            e.printStackTrace();

        }


    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {

            case CheckoutActivity.RESULT_OK:
                /* transaction completed */
                Transaction transaction = data.getParcelableExtra(CheckoutActivity.CHECKOUT_RESULT_TRANSACTION);
                /* resource path if needed */
                // String resourcePath = data.getStringExtra(CheckoutActivity.CHECKOUT_RESULT_RESOURCE_PATH);
                if (transaction.getTransactionType() == TransactionType.SYNC) {
                    /* check the result of synchronous transaction */
                    success("SYNC");
                } else {
                    /* wait for the asynchronous transaction callback in the onNewIntent() */
                }
                break;

            case CheckoutActivity.RESULT_CANCELED:
                /* shopper canceled the checkout process */
                error("2","Canceled","");
                break;

            case CheckoutActivity.RESULT_ERROR:
                /* shopper error the checkout process */
                error("3","Checkout Result Error","");
        }
    }


    @Override
    public void success(final Object result) {
        handler.post(
                () -> Result.success(result));
    }

    @Override
    public void error(
            @NonNull final String errorCode, final String errorMessage, final Object errorDetails) {
        handler.post(
                () -> Result.error(errorCode, errorMessage, errorDetails));
    }

    @Override
    public void notImplemented() {
        handler.post(
                () -> Result.notImplemented());
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        // TO BACK TO VIEW
        if (intent.getScheme().equals(ShopperResultUrl)) {
            success("success");
        }
    }

    @Override
    public void transactionCompleted(@NonNull Transaction transaction) {

        if (transaction.getTransactionType() == TransactionType.SYNC) {
            success("SYNC");
        } else {
            /* wait for the callback in the s */
            Uri uri = Uri.parse(transaction.getRedirectUrl());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    @Override
    public void transactionFailed(@NonNull Transaction transaction, @NonNull PaymentError paymentError) {
        error(
                "transactionFailed",
                paymentError.getErrorMessage(),
                "transactionFailed"
        );
    }

    @Override
    public void brandsValidationRequestSucceeded(@NonNull BrandsValidation brandsValidation) {

    }

    @Override
    public void brandsValidationRequestFailed(@NonNull PaymentError paymentError) {

    }

    @Override
    public void imagesRequestSucceeded(@NonNull ImagesRequest imagesRequest) {

    }

    @Override
    public void imagesRequestFailed() {

    }

    @Override
    public void paymentConfigRequestSucceeded(@NonNull CheckoutInfo checkoutInfo) {

    }

    @Override
    public void paymentConfigRequestFailed(@NonNull PaymentError paymentError) {

    }
}
