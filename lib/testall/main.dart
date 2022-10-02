import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_hyperpay/flutter_hyperpay.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        // This is the theme of your application.
        //
        // Try running your application with "flutter run". You'll see the
        // application has a blue toolbar. Then, without quitting the app, try
        // changing the primarySwatch below to Colors.green and then invoke
        // "hot reload" (press "r" in the console where you ran "flutter run",
        // or simply save your changes to "hot reload" in a Flutter IDE).
        // Notice that the counter didn't reset back to zero; the application
        // is not restarted.
        primarySwatch: Colors.blue,
      ),
      home: const MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}


class MyHomePage extends StatefulWidget {
  const MyHomePage({Key? key,required  this.title}) : super(key: key);

  // This widget is the home page of your application. It is stateful, meaning
  // that it has a State object (defined below) that contains fields that affect
  // how it looks.

  // This class is the configuration for the state. It holds the values (in this
  // case the title) provided by the parent (in this case the App widget) and
  // used by the build method of the State. Fields in a Widget subclass are
  // always marked "final".

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  int _counter = 0;

  void _incrementCounter() {
    setState(() {
      // This call to setState tells the Flutter framework that something has
      // changed in this State, which causes it to rerun the build method below
      // so that the display can reflect the updated values. If we changed
      // _counter without calling setState(), then the build method would not be
      // called again, and so nothing would appear to happen.
      _counter++;
    });
  }

  @override
  Widget build(BuildContext context) {
    // This method is rerun every time setState is called, for instance as done
    // by the _incrementCounter method above.
    //
    // The Flutter framework has been optimized to make rerunning build methods
    // fast, so that you can just rebuild anything that needs updating rather
    // than having to individually change instances of widgets.
    return Scaffold(
      appBar: AppBar(
        // Here we take the value from the MyHomePage object that was created by
        // the App.build method, and use it to set our appbar title.
        title: Text(widget.title),
      ),
      body: Center(
        // Center is a layout widget. It takes a single child and positions it
        // in the middle of the parent.
        child: Column(
          // Column is also a layout widget. It takes a list of children and
          // arranges them vertically. By default, it sizes itself to fit its
          // children horizontally, and tries to be as tall as its parent.
          //
          // Invoke "debug painting" (press "p" in the console, choose the
          // "Toggle Debug Paint" action from the Flutter Inspector in Android
          // Studio, or the "Toggle Debug Paint" command in Visual Studio Code)
          // to see the wireframe for each widget.
          //
          // Column has various properties to control how it sizes itself and
          // how it positions its children. Here we use mainAxisAlignment to
          // center the children vertically; the main axis here is the vertical
          // axis because Columns are vertical (the cross axis would be
          // horizontal).
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            const Text(
              'You have pushed the button this many times:',
            ),
            Text(
              '$_counter',
              style: Theme.of(context).textTheme.headline4,
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: goToHayperPayment,
        //       (){
        // Navigator.push(
        //   context,
        //   MaterialPageRoute(
        //     builder: (context) => Ready_UI(),
        //   ), ); },
        tooltip: 'Increment',
        child: const Icon(Icons.add),
      ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }

  Future<void> goToHayperPayment() async {


    FlutterHyperpay flutterHyperpay =  FlutterHyperpay(
      channeleName: InAppPaymentSetting.channel,
      shopperResultUrl: InAppPaymentSetting.ShopperResultUrl,
      paymentMode: true ? PaymentMode.TEST : PaymentMode.LIVE,
      lang: InAppPaymentSetting.getLang(),

    );

    PaymentResultData paymentResultData = await flutterHyperpay.readyUICards(
      readyUI: ReadyUI(
        brandName: "VISA",
        checkoutid: "54E87DEDF13DB506C62C4E773CF85D79.uat01-vm-tx02",
        setStorePaymentDetailsMode: true,
      ),
    );


  }



}

class InAppPaymentSetting {
  static  String paymentMode=true?"TEST":"LIVE";
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
  static const String ShopperResultUrl="com.eitinaa.payment";
  static const String ApplePaybundel="merchant.com.eitinaatalents.applepay.live";
  static const String TestMode="TEST";
  static const String LiveMode="LIVE";
  static const String CountryCode="SA";
  static const String CurrencyCode="SAR";
  static const String channel="Hyperpay.demo.fultter/channel";
  static getLang(){
    if(Platform.isIOS){
      return true?"ar":"en";
    }else{
      return true?"ar_AR":"en_US";
    }
  }
}



// import 'dart:convert';
// import 'package:flutter/material.dart';
// import 'package:flutter/services.dart';
// import 'package:flutter_demo/custom_ui.dart';
// import 'package:http/http.dart' as http;
// import 'package:flutter_demo/ready_ui.dart';
//
// main() => runApp(MyApp());
//
// class MyApp extends StatelessWidget {
//   @override
//   Widget build(BuildContext context) {
//     return MaterialApp(
//       home: HomeScreen(),
//     );
//   }
// }
//
//
//
//
// class HomeScreen extends StatefulWidget {
//   @override
//   _HomeScreenState createState() => _HomeScreenState();
// }
//
//
//
//
// class _HomeScreenState extends State<HomeScreen> {
//
//   @override
//   Widget build(BuildContext context) {
//     return Material(
//       child: Scaffold(
//         appBar: AppBar(
//           title: Text('Hyperpay Demo'),
//         ),
//         body: Center(
//           child: Column(
//             mainAxisAlignment: MainAxisAlignment.center,
//             children: [
//               Text(
//                 "Hyperpay Flutter Demo",
//                 style: TextStyle(fontSize: 18),
//               ),
//               SizedBox(height: 20),
//               ElevatedButton(
//                 child: Text('Ready UI'),
//                 onPressed: () {
//                   Navigator.push(
//                     context,
//                     MaterialPageRoute(
//                       builder: (context) => Ready_UI(),
//                     ), ); },
//                 // color: Colors.orange,
//                 // padding: EdgeInsets.fromLTRB(22, 0, 22, 0),
//               ),
//               SizedBox(height: 20),
//               ElevatedButton(
//                 child: Text('Custom UI'),
//                 onPressed: () {
//                   Navigator.push(
//                     context,
//                     MaterialPageRoute(builder: (context) => custom_UI()),
//                   );
//                 },
//                 // color: Colors.blue,
//               ),
//
//             ],
//           ),
//         ),
//       ),
//     );
//     ;
//   }
//
//   // Get battery level.
//
//
//
// }
