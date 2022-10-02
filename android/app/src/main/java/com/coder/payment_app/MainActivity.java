package com.coder.payment_app; // must be identical with this

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
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
import com.oppwa.mobile.connect.provider.Transaction;
import com.oppwa.mobile.connect.provider.TransactionType;
import com.oppwa.mobile.connect.service.ConnectService;
import com.oppwa.mobile.connect.service.IProviderBinder;

import java.util.LinkedHashSet;
import java.util.Set;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity implements ITransactionListener, MethodChannel.Result {


    private String CHANNEL = "Hyperpay.demo.fultter/channel";
    private String CustomCHANNEL = "Hyperpay.demo.custom.fultter/channel";
    private String checkoutid = "";
    private MethodChannel.Result Result;
    private String type = "";
    private String number, holder, cvv, year, month, brand;
    private IProviderBinder binder;
    private String mode = "";
    private String STCPAY = "";
    private String EnabledTokenization = "";
    private String PayTypeSotredCard = "";
    private String ShopperResultUrl = "com.pay.payment";// here is identical with
    private String TokenID = "";
    private String setStorePaymentDetailsMode = "";
    private String Lang = "";
    private Handler handler = new Handler(Looper.getMainLooper());


    Transaction transaction = null;
    String MadaRegex = "";
    String ptMadaVExp = "";
    String ptMadaMExp = "";
    String brands = "";


    boolean check(String ccNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = ccNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(ccNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);

    }


    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL).setMethodCallHandler(
                new MethodChannel.MethodCallHandler() {
                    @Override
                    public void onMethodCall(MethodCall call, MethodChannel.Result result) {

                        Result = result;
                        if (call.method.equals("gethyperpayresponse")) {

                            type = call.argument("type");
                            mode = call.argument("mode");
                            checkoutid = call.argument("checkoutid");
                            Lang = call.argument("lang");
                            ShopperResultUrl = call.argument("ShopperResultUrl");
                            brands = call.argument("brand");
                            setStorePaymentDetailsMode = call.argument("setStorePaymentDetailsMode");
                            if (type.equals("ReadyUI")) {
                                openCheckoutUI(checkoutid);
                            } else if (type.equals("StoredCards")) {
                                cvv = call.argument("cvv");
                                TokenID = call.argument("TokenID");
                                storedCardPayment(checkoutid);
                            } else {
                                brands = call.argument("brand");
                                STCPAY = call.argument("STCPAY");
                                number = call.argument("card_number");
                                holder = call.argument("holder_name");
                                year = call.argument("year");
                                month = call.argument("month");
                                cvv = call.argument("cvv");
                                ptMadaVExp = call.argument("MadaRegexV");
                                ptMadaMExp = call.argument("MadaRegexM");
                                EnabledTokenization = call.argument("EnabledTokenization");
                                PayTypeSotredCard = call.argument("PayTypeSotredCard");
                                TokenID = call.argument("TokenID");
                                Log.e("errorrr", "cheeeeeeeeeeeee" + PayTypeSotredCard);
                                openCustomUI(checkoutid);
                            }
                        } else {
                            error("1", "Method name is not found", "");
                        }
                    }
                });


        // new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL).setMethodCallHandler(hand);
    }


    private void openCheckoutUI(String checkoutId) {

        Set<String> paymentBrands = new LinkedHashSet<String>();
        //check cards
        if (brands.equals("MADA")) {
            paymentBrands.add("MADA");
        } else if (brands.equals("STC_PAY")) {
            paymentBrands.add("STC_PAY");
        } else if (brands.equals("VISA")) {
            paymentBrands.add("VISA");
        } else if (brands.equals("MASTER")) {
            paymentBrands.add("MASTER");
        } else {
            paymentBrands.add("MASTER");
            paymentBrands.add("VISA");
        }

        CheckoutSettings checkoutSettings;
        //check mode
        if (mode.equals("LIVE")) {
            //Set live mode
            checkoutSettings = new CheckoutSettings(checkoutId, paymentBrands,
                    Connect.ProviderMode.LIVE);
            Log.e("errorrr", "LIVE MODE");
        } else {
            //set test mode
            checkoutSettings = new CheckoutSettings(checkoutId, paymentBrands,
                    Connect.ProviderMode.TEST);
            Log.e("errorrr", "TEST MODE");
        }
        //set lang
        checkoutSettings.setLocale(Lang);
        Log.e("errorrr", "lang is  " + Lang);
        if (brands.equals("STC_PAY")) {
            Log.e("errorrr", "brand is  " + brands);
        } else {
            checkoutSettings.getStorePaymentDetailsMode();
        }
        //show total
        checkoutSettings.setTotalAmountRequired(true);
        //set shopper
        checkoutSettings.setShopperResultUrl(ShopperResultUrl + "://result");

        //save for next payment
        if (setStorePaymentDetailsMode.equals("true")) {
            checkoutSettings.setStorePaymentDetailsMode(CheckoutStorePaymentDetailsMode.PROMPT);
        }

        //change Theme
        checkoutSettings.setThemeResId(R.style.NewCheckoutTheme);
        ComponentName componentName = new ComponentName(
                getPackageName(), CheckoutBroadcastReceiver.class.getName());


        /* Set up the Intent and start the checkout activity. */
        Intent intent = checkoutSettings.createCheckoutActivityIntent(this, componentName);

        startActivityForResult(intent, CheckoutActivity.REQUEST_CODE_CHECKOUT);
    }

    private void storedCardPayment(String checkoutid) {

        Log.e("errorrr", "stored Card Payment = >>>>> ");

        try {
            Log.e("errorrr", "checkoutid = > " + checkoutid);
            Log.e("errorrr", "TokenID = > " + TokenID);
            Log.e("errorrr", "brand = > " + brands);
            Log.e("errorrr", "cvv = >" + cvv);

            TokenPaymentParams paymentParams = new TokenPaymentParams(checkoutid, TokenID, brands, cvv);

            Log.e("errorrr", "checkoutid = > " + checkoutid);
            Log.e("errorrr", "TokenID = > " + TokenID);
            Log.e("errorrr", "brand = > " + brands);
            Log.e("errorrr", "cvv = >" + cvv);
            Log.e("errorrr", "moooooooode = >" + mode);


            paymentParams.setShopperResultUrl(ShopperResultUrl + "://result");


            Transaction transaction = new Transaction(paymentParams);
            //Set Mode;
            boolean modeTemp = mode.equals("TEST") ? true : false;
            if (modeTemp) {
                binder.initializeProvider(Connect.ProviderMode.TEST);
            } else {
                binder.initializeProvider(Connect.ProviderMode.LIVE);
            }

            Log.e("errorrr", "Mode = >>>>> " + binder.getProviderMode().toString());
            Log.e("errorrr", "Stored Card payment = >>>>> " + TokenID.toString());
            //Submit Transaction
            binder.submitTransaction(transaction);

        } catch (PaymentException e) {
            e.printStackTrace();

        }


    }

    private void openCustomUI(String checkoutid) {

        Toast.makeText(getBaseContext(), "Waiting..", Toast.LENGTH_LONG).show();

//        if (STCPAY.equals("enabled")) {
        if (brands.equals("STC_PAY")) {
            Toast.makeText(getBaseContext(), "I am STC_PAY", Toast.LENGTH_LONG).show();
            Log.e("errorrr", "STC_PAY enter");
            try {


//                boolean modeTemp = mode.equals("TEST") ? true : false;
//                if (modeTemp) {
//                    binder.initializeProvider(Connect.ProviderMode.TEST);
//                } else {
//                    binder.initializeProvider(Connect.ProviderMode.LIVE);
//                }
                binder.initializeProvider(Connect.ProviderMode.LIVE);
                //  PaymentParams paymentParams = new PaymentParams(checkoutid, "STC_PAY");
                //paymentParams.setShopperResultUrl(ShopperResultUrl + "://result");
                STCPayPaymentParams stcPayPaymentParams = new STCPayPaymentParams(checkoutid, STCPayVerificationOption.MOBILE_PHONE);
                stcPayPaymentParams.setMobilePhoneNumber("0588638638");
                stcPayPaymentParams.setShopperResultUrl(ShopperResultUrl + "://result");
                ;

                Log.e("errorrr", "STC_PAY enter" + stcPayPaymentParams.getCheckoutId().toString());
                Log.e("errorrr", "STC_PAY enter" + stcPayPaymentParams.getPaymentBrand().toString());
                Log.e("errorrr", "STC_PAY enter" + stcPayPaymentParams.getParamsForRequest().toString());
                Transaction transaction = new Transaction(stcPayPaymentParams);

                binder.submitTransaction(transaction);
            } catch (PaymentException e) {
                e.printStackTrace();
                Log.e("errorrr", "STC_PAY enter" + e.getMessage());
            }
        } else {


            if (PayTypeSotredCard.equals("PayTypeSotredCard")) {
                Log.e("errorrr", "PayTypeSotredCard = >>>>> ");

                try {
                    Log.e("errorrr", "checkoutid = > " + checkoutid);
                    Log.e("errorrr", "TokenID = > " + TokenID);
                    Log.e("errorrr", "brand = > " + brands);
                    Log.e("errorrr", "cvv = >" + cvv);

                    TokenPaymentParams paymentParams = new TokenPaymentParams(checkoutid, TokenID, brands, cvv);

                    Log.e("errorrr", "checkoutid = > " + checkoutid);
                    Log.e("errorrr", "TokenID = > " + TokenID);
                    Log.e("errorrr", "brand = > " + brands);
                    Log.e("errorrr", "cvv = >" + cvv);


                    paymentParams.setShopperResultUrl(ShopperResultUrl + "://result");


                    Transaction transaction = new Transaction(paymentParams);

                    //Set Mode;
                    boolean modeTemp = mode.equals("TEST") ? true : false;
                    if (modeTemp) {
                        binder.initializeProvider(Connect.ProviderMode.TEST);
                    } else {
                        binder.initializeProvider(Connect.ProviderMode.LIVE);
                    }

                    Log.e("errorrr", "Mode = >>>>> " + binder.getProviderMode().toString());
                    Log.e("errorrr", "Stored Card payment = >>>>> " + TokenID.toString());
                    //Submit Transaction
                    binder.submitTransaction(transaction);

                } catch (PaymentException e) {
                    e.printStackTrace();
                }

            } else {
                boolean result = check(number);
                if (!result) {

                    Toast.makeText(getBaseContext(), "Card Number is Invalid", Toast.LENGTH_LONG).show();


                } else if (!CardPaymentParams.isNumberValid(number)) {

                    Toast.makeText(getBaseContext(), "Card Number is Invalid", Toast.LENGTH_LONG).show();
                } else if (!CardPaymentParams.isHolderValid(holder)) {

                    Toast.makeText(getBaseContext(), "Card Holder is Invalid", Toast.LENGTH_LONG).show();

                } else if (!CardPaymentParams.isExpiryYearValid(year)) {

                    Toast.makeText(getBaseContext(), "Expiry Year is Invalid", Toast.LENGTH_LONG).show();

                } else if (!CardPaymentParams.isExpiryMonthValid(month)) {
                    Toast.makeText(getBaseContext(), "Expiry Month is Invalid", Toast.LENGTH_LONG).show();
                } else if (!CardPaymentParams.isCvvValid(cvv)) {
                    Toast.makeText(getBaseContext(), "CVV is Invalid", Toast.LENGTH_LONG).show();
                } else {
                    String firstnumber = String.valueOf(number.charAt(0));
                    // To add MADA
                    if (brands.equals("mada")) {
                        String bin = number.substring(0, 6);
                        if (bin.matches(ptMadaVExp) || bin.matches(ptMadaMExp)) {
                            brand = "MADA";
                        } else {
                            Toast.makeText(MainActivity.this, "This card is not Mada card", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        if (firstnumber.equals("4")) {
                            brand = "VISA";
                        } else if (firstnumber.equals("5")) {
                            brand = "MASTER";
                        }
                    }
                    boolean EnabledTokenizationTemp = EnabledTokenization.equals("true") ? true : false;
                    Log.e("errorrr", "EnabledTokenizationTemp  =>>>> " + EnabledTokenization);
                    try {
                        PaymentParams paymentParams = new CardPaymentParams(
                                checkoutid,
                                brand,
                                number,
                                holder,
                                month,
                                year,
                                cvv
                        ).setTokenizationEnabled(EnabledTokenizationTemp);//Set Enabled TokenizationTemp
                        paymentParams.setShopperResultUrl(ShopperResultUrl + "://result");
                        Transaction transaction = new Transaction(paymentParams);

                        //Set Mode;
                        boolean modeTemp = mode.equals("TEST") ? true : false;
                        if (modeTemp) {
                            binder.initializeProvider(Connect.ProviderMode.TEST);
                        } else {
                            binder.initializeProvider(Connect.ProviderMode.LIVE);
                        }

                        Log.e("errorrr", "Mode = >>>>> " + binder.getProviderMode().toString());
                        //Submit Transaction
                        binder.submitTransaction(transaction);

                    } catch (PaymentException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (IProviderBinder) service;
            binder.addTransactionListener(MainActivity.this);

            /* we have a connection to the service */


            try {

                if (mode.equals("LIVE")) {
                    Log.e("errorrr", "livvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvee");

                    binder.initializeProvider(Connect.ProviderMode.LIVE);
                } else {
                    Log.e("errorrr", "tesssssssssssssssssssssssssssssssssssssssssssst");
                    binder.initializeProvider(Connect.ProviderMode.TEST);

                }

            } catch (PaymentException ee) {

                Log.e("PaymentException", "errrrrrrrrrrrrrrrrrrrrrrPaymentExceptionerrrrrrrrrrrrrr");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder = null;
        }
    };


    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, ConnectService.class);

        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);


    }

    @Override
    public void brandsValidationRequestSucceeded(BrandsValidation brandsValidation) {

    }

    @Override
    public void brandsValidationRequestFailed(PaymentError paymentError) {

    }

    @Override
    public void imagesRequestSucceeded(ImagesRequest imagesRequest) {

    }

    @Override
    public void imagesRequestFailed() {

    }

    @Override
    public void paymentConfigRequestSucceeded(CheckoutInfo checkoutInfo) {

    }

    @Override
    public void paymentConfigRequestFailed(PaymentError paymentError) {

    }

    @Override
    public void success(final Object result) {
        handler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        Log.e("errorrr", "Result = > " + result.toString());
                        try {
                            Result.success(result);
                        } catch (Exception e) {
                            Log.e("Exception", e.getMessage());

                        }

                    }
                });
    }

    @Override
    public void error(
            final String errorCode, final String errorMessage, final Object errorDetails) {
        handler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        Log.e("error", errorCode.toString() +
                                errorMessage.toString() + errorDetails.toString());
                        Result.error(errorCode, errorMessage, errorDetails);
                    }
                });
    }

    @Override
    public void notImplemented() {
        handler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        Result.notImplemented();
                    }
                });
    }


    @Override
    public void transactionCompleted(Transaction transaction) {


        if (transaction == null) {

            Log.e("error", "transaction return");
            return;
        }

        if (transaction.getTransactionType() == TransactionType.SYNC) {

            Log.e("error", "transaction success('sync')");

            success("SYNC");


        } else {
            /* wait for the callback in the s */

            Uri uri = Uri.parse(transaction.getRedirectUrl());

            Intent intent2 = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent2);
        }

    }

    @Override
    public void transactionFailed(Transaction transaction, PaymentError paymentError) {


        Log.e("errorrr", paymentError.getErrorMessage());
        Log.e("errorrr", paymentError.getErrorMessage());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.e("error", "Print Result Code" + resultCode + "" + requestCode);

        switch (resultCode) {
            case CheckoutActivity.RESULT_OK:
                /* transaction completed */

                Transaction transaction = data.getParcelableExtra(CheckoutActivity.CHECKOUT_RESULT_TRANSACTION);

                /* resource path if needed */
                String resourcePath = data.getStringExtra(CheckoutActivity.CHECKOUT_RESULT_RESOURCE_PATH);

                if (transaction.getTransactionType() == TransactionType.SYNC) {
                    /* check the result of synchronous transaction */

                    success("SYNC");


                } else {
                    /* wait for the asynchronous transaction callback in the onNewIntent() */
                }

                break;
            case CheckoutActivity.RESULT_CANCELED:
                /* shopper canceled the checkout process */

                Toast.makeText(getBaseContext(), "canceled", Toast.LENGTH_LONG).show();

                error("2", "Canceled", "");

                break;
            case CheckoutActivity.RESULT_ERROR:
                /* error occurred */

                PaymentError error = data.getParcelableExtra(CheckoutActivity.CHECKOUT_RESULT_ERROR);

                Toast.makeText(getBaseContext(), "error", Toast.LENGTH_LONG).show();

                Log.e("errorrr", String.valueOf(error.getErrorInfo()));

                Log.e("errorrr2", String.valueOf(error.getErrorCode()));

                Log.e("errorrr3", String.valueOf(error.getErrorMessage()));

                Log.e("errorrr4", String.valueOf(error.describeContents()));

                error("3", "Checkout Result Error", "");

        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);


        if (intent.getScheme().equals(ShopperResultUrl)) {

            success("success");

        }
    }


}

