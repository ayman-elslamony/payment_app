import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_hyperpay/flutter_hyperpay.dart';
import 'package:http/http.dart' as http;

class Ready_UI extends StatefulWidget {


  @override
  _Ready_UIState createState() => _Ready_UIState();
}

String _checkoutid = '';
String _resultText = '';

class _Ready_UIState extends State<Ready_UI> {
 late FlutterHyperpay flutterHyperPay ;
  @override
  void initState() {
    // TODO: implement initState

     flutterHyperPay = FlutterHyperpay(
      channeleName: InAppPaymentSetting.channel,
      shopperResultUrl: InAppPaymentSetting.ShopperResultUrl,
      paymentMode:  PaymentMode.TEST ,
      lang: InAppPaymentSetting.getLang(),
    );

    super.initState();
  }

  static const platform = const MethodChannel('Hyperpay.demo.fultter/channel');

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: Text('READY UI'),
          leading: IconButton(
            icon: Icon(Icons.arrow_back, color: Colors.white),
            onPressed: () => Navigator.of(context).pop(),
          ),
        ),
        body: Padding(
          padding: const EdgeInsets.all(10.0),
          child: Center(
            child: SingleChildScrollView(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  ElevatedButton(
                    child: Text('Credit Card'),
                    onPressed: () {
                      _checkoutpage("credit");
                    },
                    // padding: EdgeInsets.fromLTRB(22, 0, 22, 0),
                  ),
                  SizedBox(height: 15),
                  ElevatedButton(
                    child: Text('visa'),
                    onPressed: () {
                      _checkoutpage("visa");
                    },
                    // padding: EdgeInsets.fromLTRB(22, 0, 22, 0),
                  ), ElevatedButton(
                    child: Text('GOOGLEPAY'),
                    onPressed: () {
                      _checkoutpage("GOOGLEPAY");
                    },
                    // padding: EdgeInsets.fromLTRB(22, 0, 22, 0),
                  ),
                  SizedBox(height: 15,),

                    ElevatedButton(
                      child: Text('APPLEPAY'),
                      onPressed: () {
                        _checkoutpage("APPLEPAY");
                      },
                      // padding: EdgeInsets.fromLTRB(22, 0, 22, 0),
                      // color: Colors.black,
                      // textColor: Colors.white,
                    ) , ElevatedButton(
                      child: Text('STC_PAY'),
                      onPressed: () {
                        _checkoutpage("STC_PAY");
                      },
                      // padding: EdgeInsets.fromLTRB(22, 0, 22, 0),
                      // color: Colors.black,
                      // textColor: Colors.white,
                    ),
                  SizedBox(height: 35),
                  Text(
                    _resultText,
                    style: TextStyle(color: Colors.green, fontSize: 20),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  Future<void> _checkoutpage(String type) async {
    //  requestCheckoutId();

    var status;

    String myUrl = "http://dev.hyperpay.com/hyperpay-demo/getcheckoutid.php";
    final response = await http.post(
      Uri.parse(myUrl),
      headers: {'Accept': 'application/json'},
    );
    status = response.body.contains('error');

    var data = json.decode(response.body);

    print(data);
    print("-------------------------");

    if (status) {
      print('data : ${data["error"]}');
    } else {
      print('data : ${data["id"]}');
      _checkoutid = '${data["id"]}';


      PaymentResultData paymentResultData;

      if (type.toLowerCase() ==
          InAppPaymentSetting.APPLEPAY.toLowerCase()) {
        paymentResultData = await flutterHyperPay.payWithApplePay(
          applePay: ApplePay(

            /// ApplePayBundel refer to Merchant ID
              applePayBundel: InAppPaymentSetting.ApplePaybundel,
              checkoutid: _checkoutid,
              cuntryCode: InAppPaymentSetting.CountryCode,
              currencyCode: InAppPaymentSetting.CurrencyCode),
        );
      } else {
        paymentResultData = await flutterHyperPay.readyUICards(
          readyUI: ReadyUI(
            brandName: type,
            checkoutid: _checkoutid,
            setStorePaymentDetailsMode: false,
          ),
        );
      }

      if (paymentResultData.paymentResult == PaymentResult.SUCCESS ||
          paymentResultData.paymentResult == PaymentResult.SYNC) {
        try {
          getpaymentstatus();
        } catch (message) {}
      } else {
        setState(() {
          _resultText = paymentResultData.errorString.toString();
        });
      }
      //
      //   String transactionStatus;
      //   try {
      //     final String result = await platform.invokeMethod('gethyperpayresponse',
      //         {"type": "ReadyUI", "mode": "TEST", "checkoutid": _checkoutid,"brand": type,
      //         });
      //     transactionStatus = '$result';
      //   } on PlatformException catch (e) {
      //     transactionStatus = "${e.message}";
      //   }
      //
      //   if (transactionStatus != null ||
      //       transactionStatus == "success" ||
      //       transactionStatus == "SYNC") {
      //     print(transactionStatus);
      //     getpaymentstatus();
      //   } else {
      //     setState(() {
      //       _resultText = transactionStatus;
      //     });
      //   }
      // }
    }
  }

  Future<void> getpaymentstatus() async {
    var status;

    String myUrl = "http://dev.hyperpay.com/hyperpay-demo/getpaymentstatus.php?id=$_checkoutid";
    final response = await http.post(
      Uri.parse(myUrl),
      headers: {'Accept': 'application/json'},
    );
    status = response.body.contains('error');

    var data = json.decode(response.body);


    print("payment_status: ${data["result"].toString()}");

    setState(() {
      _resultText = data["result"].toString();
    });


  }
}




class InAppPaymentSetting {
  // static const String paymentMode="TEST";
  static const String MADA="MADA";
  static const String APPLEPAY="APPLEPAY";
  static const String Credit="credit";
  static const String STC_PAY="STC_PAY";
  static const String ReadyUI="ReadyUI";
  static const String CustomUI="CustomUI";
  static const String gethyperpayresponse="gethyperpayresponse";
  static const String success="success";
  static const String SYNC="SYNC";
  static const String PayTypeSotredCard="PayTypeSotredCard";
  static const String PayTypeFromInput="PayTypeFromInput";
  static const String EnabledTokenization="true";
  static const String DisableTokenization="false";
  static const String ShopperResultUrl="com.mosab.demohyperpayapp";
  static const String TestMode="TEST";
  static const String LiveMode="LIVE";
  static const String ApplePaybundel="merchant.com.eitinaatalents.applepay.live";
  static const String CountryCode="SA";
  static const String CurrencyCode="SAR";
  static const String channel="Hyperpay.demo.fultter/channel";
  static getLang(){



      return "ar_AR";

  }
}


class PaymentType {
  static const int HourlyContract = 1;
  static const int FlexibleService = 2;
  static const int IndividualContractRequest = 3;
  static const int IndividualContract = 4;
  static const int RenewIndividualContract = 5;
  static const int FinancialRequest = 6;
  static const int Enterprise = 7;
  static const int IndvProcedure = 8;
  static const int Points = 20;
}