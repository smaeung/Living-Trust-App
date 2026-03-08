import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  TextInput,
  Alert,
  ActivityIndicator,
  Platform,
  Linking,
} from 'react-native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RouteProp } from '@react-navigation/native';
import axios from 'axios';
import { RootStackParamList } from '../../App';

const API_BASE = Platform.select({
  android: 'http://10.0.2.2:3001',
  default: 'http://localhost:3001',
});

type PaymentScreenProps = {
  navigation: NativeStackNavigationProp<RootStackParamList, 'Payment'>;
  route: RouteProp<RootStackParamList, 'Payment'>;
};

type PaymentStep = 'form' | 'processing' | 'success' | 'error';

export default function PaymentScreen({ navigation, route }: PaymentScreenProps) {
  const { trustData, amount, displayPrice } = route.params;

  const [step, setStep] = useState<PaymentStep>('form');
  const [clientSecret, setClientSecret] = useState('');
  const [paymentIntentId, setPaymentIntentId] = useState('');
  const [downloadToken, setDownloadToken] = useState('');
  const [downloadUrlBase64, setDownloadUrlBase64] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  // Simulated card form fields (Stripe Elements handles real card capture in production)
  const [cardNumber, setCardNumber] = useState('');
  const [expiry, setExpiry] = useState('');
  const [cvc, setCvc] = useState('');
  const [nameOnCard, setNameOnCard] = useState('');
  const [email, setEmail] = useState('');
  const [creatingIntent, setCreatingIntent] = useState(false);

  useEffect(() => {
    createPaymentIntent();
  }, []);

  const createPaymentIntent = async () => {
    setCreatingIntent(true);
    try {
      const res = await axios.post(`${API_BASE}/api/payments/create-intent`, {
        trustName: trustData.trustName,
        customerEmail: email || undefined,
      });
      setClientSecret(res.data.clientSecret);
      setPaymentIntentId(res.data.paymentIntentId);
    } catch (err: any) {
      Alert.alert(
        'Payment Setup Failed',
        err.response?.data?.error || 'Unable to initialize payment. Please try again.',
      );
    } finally {
      setCreatingIntent(false);
    }
  };

  const formatCardNumber = (text: string) => {
    const cleaned = text.replace(/\D/g, '').slice(0, 16);
    const groups = cleaned.match(/.{1,4}/g) || [];
    return groups.join(' ');
  };

  const formatExpiry = (text: string) => {
    const cleaned = text.replace(/\D/g, '').slice(0, 4);
    if (cleaned.length >= 3) {
      return `${cleaned.slice(0, 2)}/${cleaned.slice(2)}`;
    }
    return cleaned;
  };

  const validateForm = (): boolean => {
    if (!nameOnCard.trim()) {
      Alert.alert('Missing Information', 'Please enter the name on your card.');
      return false;
    }
    if (!email.trim() || !email.includes('@')) {
      Alert.alert('Missing Information', 'Please enter a valid email address for your receipt.');
      return false;
    }
    const rawCard = cardNumber.replace(/\s/g, '');
    if (rawCard.length < 16) {
      Alert.alert('Invalid Card', 'Please enter a complete 16-digit card number.');
      return false;
    }
    if (expiry.length < 5) {
      Alert.alert('Invalid Expiry', 'Please enter a valid expiry date (MM/YY).');
      return false;
    }
    if (cvc.length < 3) {
      Alert.alert('Invalid CVC', 'Please enter a valid 3-digit CVC code.');
      return false;
    }
    return true;
  };

  const handlePayment = async () => {
    if (!validateForm()) return;
    if (!paymentIntentId) {
      Alert.alert('Payment Not Ready', 'Please wait while we set up your payment.');
      return;
    }

    setStep('processing');

    try {
      // In production with @stripe/stripe-react-native, you would call:
      //   const { paymentIntent, error } = await confirmPayment(clientSecret, { ... });
      // Here we simulate the confirmation flow, calling our backend confirm endpoint.
      // For development/test mode, the backend accepts mock payment intent IDs.

      const confirmRes = await axios.post(`${API_BASE}/api/payments/confirm`, {
        paymentIntentId,
        trustData,
      });

      if (confirmRes.data.success) {
        setDownloadToken(confirmRes.data.downloadToken);
        setDownloadUrlBase64(`${API_BASE}${confirmRes.data.downloadUrlBase64}`);
        setStep('success');
      } else {
        setErrorMessage('Payment confirmation failed. Please try again.');
        setStep('error');
      }
    } catch (err: any) {
      const msg = err.response?.data?.error || err.message || 'Payment failed. Please try again.';
      setErrorMessage(msg);
      setStep('error');
    }
  };

  const handleDownload = async () => {
    if (!downloadToken) return;

    Alert.alert(
      'Download Document',
      'Your official Living Trust PDF (without watermark) will open in your browser.',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Open PDF',
          onPress: async () => {
            const pdfUrl = `${API_BASE}/api/pdf/download/${downloadToken}`;
            const supported = await Linking.canOpenURL(pdfUrl);
            if (supported) {
              await Linking.openURL(pdfUrl);
            } else {
              Alert.alert('Error', 'Cannot open PDF. Please copy this URL:\n\n' + pdfUrl);
            }
          },
        },
      ],
    );
  };

  const handleRetry = () => {
    setStep('form');
    setErrorMessage('');
    createPaymentIntent();
  };

  // ── Success Screen ──
  if (step === 'success') {
    return (
      <ScrollView style={styles.container} contentContainerStyle={styles.centered}>
        <View style={styles.successCard}>
          <Text style={styles.successIcon}>🎉</Text>
          <Text style={styles.successTitle}>Payment Successful!</Text>
          <Text style={styles.successSubtitle}>
            Your Living Trust document is ready for download.
          </Text>

          <View style={styles.successDetails}>
            <Text style={styles.detailLabel}>Document</Text>
            <Text style={styles.detailValue}>{trustData.trustName}</Text>
            <Text style={styles.detailLabel}>Amount Paid</Text>
            <Text style={styles.detailValue}>{displayPrice}</Text>
            <Text style={styles.detailLabel}>Download Valid For</Text>
            <Text style={styles.detailValue}>24 hours</Text>
          </View>

          <TouchableOpacity style={styles.downloadButton} onPress={handleDownload}>
            <Text style={styles.downloadButtonText}>⬇️  Download Official PDF</Text>
          </TouchableOpacity>

          <Text style={styles.noWatermarkNote}>
            ✅ No watermark · Official document · Ready to print and sign
          </Text>

          <TouchableOpacity
            style={styles.doneButton}
            onPress={() => navigation.navigate('Home')}
          >
            <Text style={styles.doneButtonText}>Done</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    );
  }

  // ── Error Screen ──
  if (step === 'error') {
    return (
      <View style={[styles.container, styles.centered]}>
        <View style={styles.errorCard}>
          <Text style={styles.errorIcon}>❌</Text>
          <Text style={styles.errorTitle}>Payment Failed</Text>
          <Text style={styles.errorMessage}>{errorMessage}</Text>
          <TouchableOpacity style={styles.retryButton} onPress={handleRetry}>
            <Text style={styles.retryButtonText}>Try Again</Text>
          </TouchableOpacity>
          <TouchableOpacity onPress={() => navigation.goBack()} style={styles.cancelLink}>
            <Text style={styles.cancelLinkText}>Go Back</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  }

  // ── Processing Screen ──
  if (step === 'processing') {
    return (
      <View style={[styles.container, styles.centered]}>
        <ActivityIndicator size="large" color="#1a365d" />
        <Text style={styles.processingText}>Processing your payment...</Text>
        <Text style={styles.processingSubtext}>Please do not close this screen</Text>
      </View>
    );
  }

  // ── Payment Form ──
  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.headerTitle}>💳 Secure Payment</Text>
        <Text style={styles.headerSubtext}>Complete your purchase to download the official document</Text>
      </View>

      {/* Order Summary */}
      <View style={styles.orderCard}>
        <Text style={styles.orderTitle}>Order Summary</Text>
        <View style={styles.orderRow}>
          <Text style={styles.orderItem}>Living Trust PDF — {trustData.trustName}</Text>
          <Text style={styles.orderPrice}>{displayPrice}</Text>
        </View>
        <View style={styles.orderDivider} />
        <View style={styles.orderRow}>
          <Text style={styles.orderTotal}>Total</Text>
          <Text style={styles.orderTotalPrice}>{displayPrice}</Text>
        </View>
        <View style={styles.stripeBadge}>
          <Text style={styles.stripeBadgeText}>🔒 Secured by Stripe</Text>
        </View>
      </View>

      {/* Card Form */}
      <View style={styles.formCard}>
        <Text style={styles.formTitle}>Payment Details</Text>

        {creatingIntent && (
          <View style={styles.initializingRow}>
            <ActivityIndicator size="small" color="#4299e1" />
            <Text style={styles.initializingText}>Initializing payment...</Text>
          </View>
        )}

        <Text style={styles.label}>Name on Card *</Text>
        <TextInput
          style={styles.input}
          placeholder="John Smith"
          placeholderTextColor="#a0aec0"
          value={nameOnCard}
          onChangeText={setNameOnCard}
          autoCapitalize="words"
        />

        <Text style={styles.label}>Email Address *</Text>
        <TextInput
          style={styles.input}
          placeholder="john@example.com"
          placeholderTextColor="#a0aec0"
          value={email}
          onChangeText={setEmail}
          keyboardType="email-address"
          autoCapitalize="none"
        />

        <Text style={styles.label}>Card Number *</Text>
        <TextInput
          style={styles.input}
          placeholder="1234 5678 9012 3456"
          placeholderTextColor="#a0aec0"
          value={cardNumber}
          onChangeText={(t) => setCardNumber(formatCardNumber(t))}
          keyboardType="number-pad"
          maxLength={19}
        />

        <View style={styles.row}>
          <View style={styles.halfField}>
            <Text style={styles.label}>Expiry *</Text>
            <TextInput
              style={styles.input}
              placeholder="MM/YY"
              placeholderTextColor="#a0aec0"
              value={expiry}
              onChangeText={(t) => setExpiry(formatExpiry(t))}
              keyboardType="number-pad"
              maxLength={5}
            />
          </View>
          <View style={[styles.halfField, { marginLeft: 12 }]}>
            <Text style={styles.label}>CVC *</Text>
            <TextInput
              style={styles.input}
              placeholder="123"
              placeholderTextColor="#a0aec0"
              value={cvc}
              onChangeText={(t) => setCvc(t.replace(/\D/g, '').slice(0, 4))}
              keyboardType="number-pad"
              maxLength={4}
              secureTextEntry
            />
          </View>
        </View>

        <View style={styles.testModeNote}>
          <Text style={styles.testModeText}>
            🧪 Test Mode: Use card 4242 4242 4242 4242 · Any future date · Any CVC
          </Text>
        </View>
      </View>

      {/* Pay Button */}
      <TouchableOpacity
        style={[styles.payButton, (creatingIntent || !paymentIntentId) && styles.payButtonDisabled]}
        onPress={handlePayment}
        disabled={creatingIntent || !paymentIntentId}
      >
        <Text style={styles.payButtonText}>
          🔒 Pay {displayPrice} — Download PDF
        </Text>
      </TouchableOpacity>

      <Text style={styles.securityNote}>
        🛡️ Your payment is encrypted and processed securely by Stripe.
        We never store your card details.
      </Text>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f5f7fa' },
  content: { paddingBottom: 40 },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 20 },
  header: {
    backgroundColor: '#1a365d',
    padding: 24,
    alignItems: 'center',
  },
  headerTitle: { fontSize: 22, fontWeight: 'bold', color: '#fff' },
  headerSubtext: { fontSize: 13, color: '#a0aec0', marginTop: 6, textAlign: 'center' },
  orderCard: {
    backgroundColor: '#fff',
    margin: 16,
    borderRadius: 14,
    padding: 18,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.07,
    shadowRadius: 6,
    elevation: 3,
  },
  orderTitle: { fontSize: 16, fontWeight: 'bold', color: '#1a365d', marginBottom: 12 },
  orderRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  orderItem: { fontSize: 14, color: '#4a5568', flex: 1, marginRight: 8 },
  orderPrice: { fontSize: 14, fontWeight: 'bold', color: '#2d3748' },
  orderDivider: { height: 1, backgroundColor: '#e2e8f0', marginVertical: 12 },
  orderTotal: { fontSize: 16, fontWeight: 'bold', color: '#1a365d' },
  orderTotalPrice: { fontSize: 22, fontWeight: 'bold', color: '#1a365d' },
  stripeBadge: {
    alignSelf: 'flex-end',
    backgroundColor: '#f7fafc',
    borderRadius: 6,
    paddingHorizontal: 8,
    paddingVertical: 4,
    marginTop: 10,
  },
  stripeBadgeText: { fontSize: 11, color: '#718096' },
  formCard: {
    backgroundColor: '#fff',
    margin: 16,
    marginTop: 0,
    borderRadius: 14,
    padding: 18,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.07,
    shadowRadius: 6,
    elevation: 3,
  },
  formTitle: { fontSize: 16, fontWeight: 'bold', color: '#1a365d', marginBottom: 16 },
  initializingRow: { flexDirection: 'row', alignItems: 'center', marginBottom: 12 },
  initializingText: { marginLeft: 8, color: '#4299e1', fontSize: 13 },
  label: { fontSize: 13, fontWeight: '600', color: '#4a5568', marginBottom: 6, marginTop: 12 },
  input: {
    backgroundColor: '#f7fafc',
    borderRadius: 10,
    padding: 13,
    fontSize: 15,
    borderWidth: 1,
    borderColor: '#e2e8f0',
    color: '#2d3748',
  },
  row: { flexDirection: 'row' },
  halfField: { flex: 1 },
  testModeNote: {
    backgroundColor: '#fffbeb',
    borderRadius: 8,
    padding: 10,
    marginTop: 16,
    borderLeftWidth: 3,
    borderLeftColor: '#f6ad55',
  },
  testModeText: { fontSize: 12, color: '#c05621' },
  payButton: {
    backgroundColor: '#48bb78',
    margin: 16,
    padding: 18,
    borderRadius: 14,
    alignItems: 'center',
    shadowColor: '#48bb78',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 5,
  },
  payButtonDisabled: { backgroundColor: '#9ae6b4' },
  payButtonText: { color: '#fff', fontWeight: 'bold', fontSize: 18 },
  securityNote: {
    fontSize: 12,
    color: '#718096',
    textAlign: 'center',
    marginHorizontal: 20,
    lineHeight: 18,
  },
  // Success styles
  successCard: {
    backgroundColor: '#fff',
    borderRadius: 20,
    padding: 28,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.1,
    shadowRadius: 12,
    elevation: 6,
    width: '100%',
  },
  successIcon: { fontSize: 60, marginBottom: 12 },
  successTitle: { fontSize: 26, fontWeight: 'bold', color: '#276749', marginBottom: 8 },
  successSubtitle: { fontSize: 15, color: '#4a5568', textAlign: 'center', marginBottom: 20 },
  successDetails: {
    alignSelf: 'stretch',
    backgroundColor: '#f7fafc',
    borderRadius: 12,
    padding: 16,
    marginBottom: 20,
  },
  detailLabel: { fontSize: 12, color: '#718096', marginTop: 8 },
  detailValue: { fontSize: 15, fontWeight: 'bold', color: '#2d3748' },
  downloadButton: {
    backgroundColor: '#1a365d',
    borderRadius: 14,
    paddingVertical: 16,
    paddingHorizontal: 32,
    alignSelf: 'stretch',
    alignItems: 'center',
    marginBottom: 12,
  },
  downloadButtonText: { color: '#fff', fontWeight: 'bold', fontSize: 17 },
  noWatermarkNote: { fontSize: 13, color: '#48bb78', marginBottom: 20, textAlign: 'center' },
  doneButton: {
    backgroundColor: '#e2e8f0',
    borderRadius: 12,
    paddingVertical: 12,
    paddingHorizontal: 40,
  },
  doneButtonText: { color: '#4a5568', fontWeight: 'bold', fontSize: 15 },
  // Error styles
  errorCard: {
    backgroundColor: '#fff',
    borderRadius: 20,
    padding: 28,
    alignItems: 'center',
    width: '100%',
  },
  errorIcon: { fontSize: 52, marginBottom: 12 },
  errorTitle: { fontSize: 22, fontWeight: 'bold', color: '#c53030', marginBottom: 8 },
  errorMessage: { fontSize: 14, color: '#4a5568', textAlign: 'center', marginBottom: 24 },
  retryButton: {
    backgroundColor: '#1a365d',
    borderRadius: 12,
    paddingVertical: 14,
    paddingHorizontal: 40,
    marginBottom: 12,
  },
  retryButtonText: { color: '#fff', fontWeight: 'bold', fontSize: 15 },
  cancelLink: { padding: 10 },
  cancelLinkText: { color: '#4299e1', fontSize: 14 },
  // Processing styles
  processingText: { marginTop: 20, fontSize: 18, fontWeight: 'bold', color: '#1a365d' },
  processingSubtext: { fontSize: 14, color: '#718096', marginTop: 8 },
});
