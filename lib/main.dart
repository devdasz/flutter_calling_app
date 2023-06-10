import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MainPage(),
    );
  }
}

class MainPage extends StatefulWidget {
  const MainPage({super.key});

  @override
  State<MainPage> createState() => _MainPageState();
}

class _MainPageState extends State<MainPage> {
  late StreamSubscription _streamSubscription;
  String batteryLevel = 'Listening...';
  String chargingStatus = "Streaming..";
  String callingStatus = "Streaming..";
  static const batteryChannel = MethodChannel('batteryChannel.com');
  static const chargingChannel = EventChannel('chargingChannel.com');
  static const callingChannel = EventChannel('callingChannel.com');

  @override
  void initState() {
    // TODO: implement initState
    super.initState();

    onListenBattery();
    onStreamBattery();
    onStreamCall();
  }

  @override
  void dispose() {
    _streamSubscription.cancel();
    super.dispose();
  }

  void onListenBattery() {
    batteryChannel.setMethodCallHandler((call) async {
      if (call.method == "reportBatteryLevel") {
        final int batteryLevel = call.arguments;
        setState(() => this.batteryLevel = '$batteryLevel');
      }
    });
  }

  void onStreamBattery() {
    _streamSubscription =
        chargingChannel.receiveBroadcastStream().listen((event) {
      setState(() {
        chargingStatus = '$event';
      });
    });
  }

  void onStreamCall() {
    _streamSubscription =
        callingChannel.receiveBroadcastStream().listen((event) {
      setState(() {
        chargingStatus = '$event';
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Container(
          width: double.infinity,
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(batteryLevel),
              Text(chargingStatus),
              Text(callingStatus),
            ],
          ),
        ),
      ),
    );
  }
}
