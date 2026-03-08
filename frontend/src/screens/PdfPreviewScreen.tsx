import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  ActivityIndicator,
  Alert,
  Platform,
} from 'react-native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RouteProp } from '@react-navigation/native';
import axios from 'axios';
import { RootStackParamList } from '../../App';

const API_BASE = Platform.select({
  android: 'http://10.0.2.2:3001',
  default: 'http://localhost:3001',
});

type PdfPreviewScreenProps = {
  navigation: NativeStackNavigationProp<RootStackParamList, 'PdfPreview'>;
  route: RouteProp<RootStackParamList, 'PdfPreview'>;
};

const STATE_OPTIONS = [
  { code: 'WA', name: 'Washington' },
  { code: 'CA', name: 'California' },
  { code: 'TX', name: 'Texas' },
  { code: 'FL', name: 'Florida' },
  { code: 'NY', name: 'New York' },
  { code: 'IL', name: 'Illinois' },
  { code: 'GA', name: 'Georgia' },
  { code: 'PA', name: 'Pennsylvania' },
  { code: 'OH', name: 'Ohio' },
  { code: 'NC', name: 'North Carolina' },
  { code: 'AZ', name: 'Arizona' },
  { code: 'NV', name: 'Nevada' },
];

export default function PdfPreviewScreen({ navigation, route }: PdfPreviewScreenProps) {
  const { trustData } = route.params;

  const [loading, setLoading] = useState(false);
  const [previewReady, setPreviewReady] = useState(false);
  const [selectedState, setSelectedState] = useState(trustData.state || 'WA');
  const [governingLaw, setGoverningLaw] = useState('');
  const [pdfPages, setPdfPages] = useState<string[]>([]);
  const [showStateSelector, setShowStateSelector] = useState(false);
  const [pricing, setPricing] = useState({ displayPrice: '$29.99', amount: 2999 });

  useEffect(() => {
    fetchPricingConfig();
  }, []);

  const fetchPricingConfig = async () => {
    try {
      const res = await axios.get(`${API_BASE}/api/payments/config`);
      setPricing(res.data.price);
    } catch {
      // Use default pricing if config fetch fails
    }
  };

  const generatePreview = async () => {
    setLoading(true);
    setPreviewReady(false);
    try {
      const res = await axios.post(`${API_BASE}/api/pdf/preview-base64`, {
        ...trustData,
        state: selectedState,
      });

      if (res.data.success) {
        setGoverningLaw(res.data.governingLaw || '');
        // Build page-summary representation (can't render PDF natively without native module)
        // Instead, show a rich text summary of document structure
        setPdfPages([
          '📄 Title Page — Trust Name & Summary',
          '📋 Article I — Identification & Governing Law',
          '🔄 Article II — Revocability & Amendment',
          '👮 Article III — Trustee Provisions & Powers',
          '💰 Article IV — Distributions During Lifetime',
          '📦 Article V — Distribution Upon Death',
          '⚖️  Article VI — General Provisions',
          '✍️  Execution & Signature Page',
          '📝 Notarization Block',
          '📂 Schedule A — Trust Property',
        ]);
        setPreviewReady(true);
      }
    } catch (err: any) {
      Alert.alert(
        'Preview Failed',
        err.response?.data?.error || 'Could not generate document preview. Please check your connection.',
      );
    } finally {
      setLoading(false);
    }
  };

  const handleProceedToPayment = () => {
    navigation.navigate('Payment', {
      trustData: { ...trustData, state: selectedState },
      amount: pricing.amount,
      displayPrice: pricing.displayPrice,
    });
  };

  const selectedStateName = STATE_OPTIONS.find(s => s.code === selectedState)?.name || selectedState;

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.headerIcon}>📄</Text>
        <Text style={styles.headerTitle}>Document Preview</Text>
        <Text style={styles.headerSubtext}>
          Review your Living Trust before purchasing
        </Text>
      </View>

      {/* State Selector */}
      <View style={styles.card}>
        <Text style={styles.cardTitle}>🗺️ Select Your State</Text>
        <Text style={styles.cardSubtitle}>
          Your document will use state-specific legal language and comply with your state's trust laws.
        </Text>

        <TouchableOpacity
          style={styles.stateButton}
          onPress={() => setShowStateSelector(!showStateSelector)}
        >
          <Text style={styles.stateButtonText}>
            {selectedStateName}  {showStateSelector ? '▲' : '▼'}
          </Text>
        </TouchableOpacity>

        {showStateSelector && (
          <View style={styles.stateList}>
            <ScrollView style={styles.stateScroll} nestedScrollEnabled>
              {STATE_OPTIONS.map(state => (
                <TouchableOpacity
                  key={state.code}
                  style={[
                    styles.stateOption,
                    selectedState === state.code && styles.stateOptionActive,
                  ]}
                  onPress={() => {
                    setSelectedState(state.code);
                    setShowStateSelector(false);
                    setPreviewReady(false);
                  }}
                >
                  <Text style={[
                    styles.stateOptionText,
                    selectedState === state.code && styles.stateOptionTextActive,
                  ]}>
                    {state.name}
                  </Text>
                  {selectedState === state.code && (
                    <Text style={styles.checkmark}>✓</Text>
                  )}
                </TouchableOpacity>
              ))}
              <TouchableOpacity
                style={styles.stateOption}
                onPress={() => {
                  Alert.alert(
                    'Other States',
                    'For states not listed, we use the Uniform Trust Code template. Select any state and our generic template will be applied.',
                  );
                  setShowStateSelector(false);
                }}
              >
                <Text style={styles.stateOptionText}>Other State (Uniform Trust Code)</Text>
              </TouchableOpacity>
            </ScrollView>
          </View>
        )}

        <TouchableOpacity
          style={[styles.generateButton, loading && styles.buttonDisabled]}
          onPress={generatePreview}
          disabled={loading}
        >
          {loading ? (
            <ActivityIndicator color="#fff" />
          ) : (
            <Text style={styles.generateButtonText}>
              {previewReady ? '🔄 Regenerate Preview' : '👁️ Generate Preview'}
            </Text>
          )}
        </TouchableOpacity>
      </View>

      {/* Trust Summary Card */}
      <View style={styles.card}>
        <Text style={styles.cardTitle}>📋 Trust Summary</Text>
        <View style={styles.summaryRow}>
          <Text style={styles.summaryLabel}>Trust Name</Text>
          <Text style={styles.summaryValue}>{trustData.trustName}</Text>
        </View>
        <View style={styles.summaryRow}>
          <Text style={styles.summaryLabel}>Grantor</Text>
          <Text style={styles.summaryValue}>{trustData.grantor}</Text>
        </View>
        <View style={styles.summaryRow}>
          <Text style={styles.summaryLabel}>Trustee</Text>
          <Text style={styles.summaryValue}>{trustData.trustee}</Text>
        </View>
        <View style={styles.summaryRow}>
          <Text style={styles.summaryLabel}>Successor Trustee</Text>
          <Text style={styles.summaryValue}>{trustData.successorTrustee}</Text>
        </View>
        <View style={styles.summaryRow}>
          <Text style={styles.summaryLabel}>Beneficiaries</Text>
          <Text style={styles.summaryValue}>
            {Array.isArray(trustData.beneficiaries)
              ? trustData.beneficiaries.join(', ')
              : trustData.beneficiaries || 'None listed'}
          </Text>
        </View>
        <View style={styles.summaryRow}>
          <Text style={styles.summaryLabel}>State</Text>
          <Text style={styles.summaryValue}>{selectedStateName}</Text>
        </View>
        {governingLaw ? (
          <View style={styles.legalBadge}>
            <Text style={styles.legalBadgeText}>⚖️ {governingLaw}</Text>
          </View>
        ) : null}
      </View>

      {/* Document Preview Structure */}
      {previewReady && (
        <View style={styles.card}>
          <View style={styles.watermarkBanner}>
            <Text style={styles.watermarkText}>👁️ PREVIEW — WATERMARKED</Text>
            <Text style={styles.watermarkSubtext}>Purchase to remove watermark and download the official document</Text>
          </View>

          <Text style={styles.cardTitle}>📑 Document Structure</Text>
          <Text style={styles.previewNote}>
            Your Living Trust document includes {pdfPages.length} sections:
          </Text>

          {pdfPages.map((page, i) => (
            <View key={i} style={styles.pageRow}>
              <View style={styles.pageNumber}>
                <Text style={styles.pageNumberText}>{i + 1}</Text>
              </View>
              <Text style={styles.pageTitle}>{page}</Text>
            </View>
          ))}

          <View style={styles.infoBox}>
            <Text style={styles.infoTitle}>✅ What's included in your document:</Text>
            <Text style={styles.infoItem}>• Full {selectedStateName} state-compliant trust language</Text>
            <Text style={styles.infoItem}>• Governing law citations ({governingLaw})</Text>
            <Text style={styles.infoItem}>• Trustee powers and succession provisions</Text>
            <Text style={styles.infoItem}>• Spendthrift & no-contest protections</Text>
            <Text style={styles.infoItem}>• Signature & notarization pages</Text>
            <Text style={styles.infoItem}>• Schedule A — property transfer sheet</Text>
          </View>
        </View>
      )}

      {/* Pricing & Purchase */}
      <View style={styles.purchaseCard}>
        <Text style={styles.priceLabel}>Official Document Price</Text>
        <Text style={styles.priceAmount}>{pricing.displayPrice}</Text>
        <Text style={styles.priceSubtext}>One-time purchase · Instant download · No subscription</Text>

        <View style={styles.featureList}>
          <Text style={styles.featureItem}>✅ No watermark</Text>
          <Text style={styles.featureItem}>✅ Printable PDF</Text>
          <Text style={styles.featureItem}>✅ Legal template compliant with {selectedStateName} law</Text>
          <Text style={styles.featureItem}>✅ Signature-ready format</Text>
          <Text style={styles.featureItem}>✅ 24-hour download link</Text>
        </View>

        <TouchableOpacity
          style={[styles.purchaseButton, !previewReady && styles.purchaseButtonDimmed]}
          onPress={previewReady ? handleProceedToPayment : () => Alert.alert('Generate Preview First', 'Please generate the preview first to see your document.')}
        >
          <Text style={styles.purchaseButtonText}>
            {previewReady ? `💳 Purchase for ${pricing.displayPrice}` : '👁️ Preview Document First'}
          </Text>
        </TouchableOpacity>

        <Text style={styles.secureNote}>🔒 Secure payment powered by Stripe</Text>
      </View>

      {/* Disclaimer */}
      <View style={styles.disclaimer}>
        <Text style={styles.disclaimerText}>
          ⚠️ This document is AI-generated from your inputs and provides a legal framework.
          It does not constitute legal advice. Consult a licensed attorney in {selectedStateName} before signing.
        </Text>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f5f7fa' },
  content: { paddingBottom: 40 },
  header: {
    backgroundColor: '#1a365d',
    padding: 28,
    alignItems: 'center',
  },
  headerIcon: { fontSize: 44, marginBottom: 8 },
  headerTitle: { fontSize: 24, fontWeight: 'bold', color: '#fff' },
  headerSubtext: { fontSize: 14, color: '#a0aec0', marginTop: 6, textAlign: 'center' },
  card: {
    backgroundColor: '#fff',
    margin: 16,
    marginBottom: 0,
    borderRadius: 14,
    padding: 18,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.07,
    shadowRadius: 6,
    elevation: 3,
  },
  cardTitle: {
    fontSize: 17,
    fontWeight: 'bold',
    color: '#1a365d',
    marginBottom: 8,
  },
  cardSubtitle: { fontSize: 13, color: '#718096', marginBottom: 14, lineHeight: 18 },
  stateButton: {
    backgroundColor: '#ebf8ff',
    borderRadius: 10,
    padding: 14,
    borderWidth: 1,
    borderColor: '#bee3f8',
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  stateButtonText: { fontSize: 16, color: '#2b6cb0', fontWeight: '600' },
  stateList: { marginTop: 8, borderWidth: 1, borderColor: '#e2e8f0', borderRadius: 10, overflow: 'hidden' },
  stateScroll: { maxHeight: 220 },
  stateOption: {
    padding: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f4f8',
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  stateOptionActive: { backgroundColor: '#ebf8ff' },
  stateOptionText: { fontSize: 15, color: '#2d3748' },
  stateOptionTextActive: { color: '#2b6cb0', fontWeight: 'bold' },
  checkmark: { color: '#48bb78', fontWeight: 'bold', fontSize: 16 },
  generateButton: {
    backgroundColor: '#4299e1',
    borderRadius: 10,
    padding: 14,
    alignItems: 'center',
    marginTop: 14,
  },
  buttonDisabled: { opacity: 0.6 },
  generateButtonText: { color: '#fff', fontWeight: 'bold', fontSize: 15 },
  summaryRow: {
    flexDirection: 'row',
    paddingVertical: 8,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f4f8',
  },
  summaryLabel: { width: 140, fontSize: 13, color: '#718096', fontWeight: '600' },
  summaryValue: { flex: 1, fontSize: 13, color: '#2d3748' },
  legalBadge: {
    backgroundColor: '#f0fff4',
    borderRadius: 8,
    padding: 10,
    marginTop: 12,
    borderLeftWidth: 3,
    borderLeftColor: '#48bb78',
  },
  legalBadgeText: { fontSize: 12, color: '#276749' },
  watermarkBanner: {
    backgroundColor: '#fff5f5',
    borderRadius: 10,
    padding: 12,
    marginBottom: 14,
    borderWidth: 1,
    borderColor: '#fed7d7',
    alignItems: 'center',
  },
  watermarkText: { fontWeight: 'bold', color: '#c53030', fontSize: 13 },
  watermarkSubtext: { fontSize: 11, color: '#e53e3e', marginTop: 4, textAlign: 'center' },
  previewNote: { fontSize: 13, color: '#4a5568', marginBottom: 12 },
  pageRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 8,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f4f8',
  },
  pageNumber: {
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: '#ebf8ff',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  pageNumberText: { color: '#2b6cb0', fontWeight: 'bold', fontSize: 12 },
  pageTitle: { fontSize: 14, color: '#2d3748', flex: 1 },
  infoBox: {
    backgroundColor: '#f0fff4',
    borderRadius: 10,
    padding: 14,
    marginTop: 14,
    borderLeftWidth: 3,
    borderLeftColor: '#48bb78',
  },
  infoTitle: { fontWeight: 'bold', color: '#276749', marginBottom: 8, fontSize: 13 },
  infoItem: { fontSize: 13, color: '#2f855a', marginBottom: 4 },
  purchaseCard: {
    backgroundColor: '#1a365d',
    margin: 16,
    borderRadius: 16,
    padding: 22,
    alignItems: 'center',
  },
  priceLabel: { color: '#a0aec0', fontSize: 13, marginBottom: 4 },
  priceAmount: { color: '#fff', fontSize: 42, fontWeight: 'bold', marginBottom: 4 },
  priceSubtext: { color: '#90cdf4', fontSize: 12, marginBottom: 16, textAlign: 'center' },
  featureList: { alignSelf: 'stretch', marginBottom: 20 },
  featureItem: { color: '#e2e8f0', fontSize: 14, marginBottom: 6 },
  purchaseButton: {
    backgroundColor: '#48bb78',
    borderRadius: 12,
    paddingVertical: 16,
    paddingHorizontal: 32,
    alignSelf: 'stretch',
    alignItems: 'center',
  },
  purchaseButtonDimmed: { backgroundColor: '#68d391' },
  purchaseButtonText: { color: '#fff', fontWeight: 'bold', fontSize: 17 },
  secureNote: { color: '#90cdf4', fontSize: 12, marginTop: 12 },
  disclaimer: {
    margin: 16,
    padding: 14,
    backgroundColor: '#fff5f5',
    borderRadius: 10,
    borderLeftWidth: 4,
    borderLeftColor: '#fc8181',
  },
  disclaimerText: { fontSize: 12, color: '#c53030', lineHeight: 18 },
});
