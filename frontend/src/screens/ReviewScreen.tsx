import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, TextInput, Alert, ActivityIndicator } from 'react-native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RouteProp } from '@react-navigation/native';
import { RootStackParamList } from '../App';

type ReviewScreenProps = {
  navigation: NativeStackNavigationProp<RootStackParamList, 'Review'>;
  route: RouteProp<RootStackParamList, 'Review'>;
};

export default function ReviewScreen({ navigation }: ReviewScreenProps) {
  const [documentText, setDocumentText] = useState('');
  const [analyzing, setAnalyzing] = useState(false);
  const [analysisResult, setAnalysisResult] = useState<any>(null);

  const handleAnalyze = () => {
    if (!documentText.trim()) {
      Alert.alert('⚠️ Please enter document text', 'Paste your Trust document below for analysis.');
      return;
    }

    setAnalyzing(true);
    
    // Simulate AI analysis
    setTimeout(() => {
      setAnalyzing(false);
      setAnalysisResult({
        score: 85,
        issues: [
          { severity: 'medium', text: 'Missing successor trustee signature line', suggestion: 'Add a signature line for the successor trustee' },
          { severity: 'low', text: 'Beneficiary designation could be more specific', suggestion: 'Consider adding more details about beneficiary distribution' },
        ],
        recommendations: [
          'Add proper notarization statement',
          'Include tax identification number for the trust',
          'Consider adding provisions for disability',
        ],
        summary: 'Your Living Trust appears to be well-structured with a few areas for improvement. The document covers the essential elements but would benefit from additional provisions.',
      });
    }, 2000);
  };

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>🔍</Text>
        <Text style={styles.headerText}>AI Document Review</Text>
        <Text style={styles.headerSubtext}>
          Paste your Living Trust document below for AI-powered analysis
        </Text>
      </View>

      <View style={styles.inputSection}>
        <Text style={styles.label}>📄 Paste Your Trust Document</Text>
        <TextInput
          style={styles.textArea}
          placeholder="Paste your Living Trust document here..."
          placeholderTextColor="#a0aec0"
          value={documentText}
          onChangeText={setDocumentText}
          multiline
          numberOfLines={15}
          textAlignVertical="top"
        />
      </View>

      <TouchableOpacity 
        style={styles.analyzeButton}
        onPress={handleAnalyze}
        disabled={analyzing}
      >
        {analyzing ? (
          <ActivityIndicator color="#fff" />
        ) : (
          <>
            <Text style={styles.analyzeButtonText}>🤖 Analyze with AI</Text>
          </>
        )}
      </TouchableOpacity>

      {analysisResult && (
        <View style={styles.resultSection}>
          <View style={styles.scoreCard}>
            <Text style={styles.scoreLabel}>Overall Score</Text>
            <Text style={styles.scoreValue}>{analysisResult.score}/100</Text>
            <View style={styles.scoreBar}>
              <View style={[styles.scoreFill, { width: `${analysisResult.score}%` }]} />
            </View>
          </View>

          <View style={styles.issuesSection}>
            <Text style={styles.sectionTitle}>⚠️ Issues Found</Text>
            {analysisResult.issues.map((issue: any, index: number) => (
              <View key={index} style={styles.issueCard}>
                <View style={styles.issueHeader}>
                  <Text style={[
                    styles.issueSeverity,
                    issue.severity === 'high' && styles.issueSeverityHigh,
                    issue.severity === 'medium' && styles.issueSeverityMedium,
                  ]}>
                    {issue.severity.toUpperCase()}
                  </Text>
                </View>
                <Text style={styles.issueText}>{issue.text}</Text>
                <Text style={styles.issueSuggestion}>💡 {issue.suggestion}</Text>
              </View>
            ))}
          </View>

          <View style={styles.recommendationsSection}>
            <Text style={styles.sectionTitle}>✅ Recommendations</Text>
            {analysisResult.recommendations.map((rec: string, index: number) => (
              <View key={index} style={styles.recItem}>
                <Text style={styles.recBullet}>•</Text>
                <Text style={styles.recText}>{rec}</Text>
              </View>
            ))}
          </View>

          <View style={styles.summarySection}>
            <Text style={styles.sectionTitle}>📋 AI Summary</Text>
            <Text style={styles.summaryText}>{analysisResult.summary}</Text>
          </View>
        </View>
      )}

      <View style={styles.disclaimer}>
        <Text style={styles.disclaimerText}>
          ⚠️ Disclaimer: This is AI-assisted analysis and does not constitute legal advice. 
          Please consult with a qualified attorney for legal matters.
        </Text>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f7fa',
  },
  header: {
    backgroundColor: '#1a365d',
    padding: 25,
    alignItems: 'center',
  },
  headerTitle: {
    fontSize: 40,
    marginBottom: 10,
  },
  headerText: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#fff',
  },
  headerSubtext: {
    fontSize: 14,
    color: '#a0aec0',
    textAlign: 'center',
    marginTop: 8,
  },
  inputSection: {
    padding: 20,
  },
  label: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#1a365d',
    marginBottom: 10,
  },
  textArea: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 15,
    fontSize: 14,
    minHeight: 200,
    borderWidth: 1,
    borderColor: '#e2e8f0',
    color: '#2d3748',
  },
  analyzeButton: {
    backgroundColor: '#48bb78',
    marginHorizontal: 20,
    padding: 16,
    borderRadius: 12,
    alignItems: 'center',
  },
  analyzeButtonText: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#fff',
  },
  resultSection: {
    padding: 20,
  },
  scoreCard: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 20,
    alignItems: 'center',
    marginBottom: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  scoreLabel: {
    fontSize: 14,
    color: '#718096',
  },
  scoreValue: {
    fontSize: 48,
    fontWeight: 'bold',
    color: '#48bb78',
    marginVertical: 10,
  },
  scoreBar: {
    width: '100%',
    height: 10,
    backgroundColor: '#e2e8f0',
    borderRadius: 5,
    overflow: 'hidden',
  },
  scoreFill: {
    height: '100%',
    backgroundColor: '#48bb78',
    borderRadius: 5,
  },
  issuesSection: {
    marginBottom: 20,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1a365d',
    marginBottom: 12,
  },
  issueCard: {
    backgroundColor: '#fff',
    borderRadius: 10,
    padding: 15,
    marginBottom: 10,
    borderLeftWidth: 4,
    borderLeftColor: '#ecc94b',
  },
  issueHeader: {
    flexDirection: 'row',
    marginBottom: 8,
  },
  issueSeverity: {
    fontSize: 12,
    fontWeight: 'bold',
    color: '#d69e2e',
    backgroundColor: '#fefcbf',
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 4,
  },
  issueSeverityHigh: {
    color: '#c53030',
    backgroundColor: '#fed7d7',
  },
  issueSeverityMedium: {
    color: '#d69e2e',
    backgroundColor: '#fefcbf',
  },
  issueText: {
    fontSize: 15,
    color: '#2d3748',
    marginBottom: 8,
  },
  issueSuggestion: {
    fontSize: 14,
    color: '#2b6cb0',
  },
  recommendationsSection: {
    marginBottom: 20,
  },
  recItem: {
    flexDirection: 'row',
    marginBottom: 8,
  },
  recBullet: {
    fontSize: 16,
    color: '#48bb78',
    marginRight: 8,
  },
  recText: {
    fontSize: 15,
    color: '#2d3748',
    flex: 1,
  },
  summarySection: {
    backgroundColor: '#ebf8ff',
    borderRadius: 12,
    padding: 16,
    marginBottom: 20,
  },
  summaryText: {
    fontSize: 15,
    color: '#2c5282',
    lineHeight: 22,
  },
  disclaimer: {
    backgroundColor: '#fff5f5',
    margin: 20,
    padding: 15,
    borderRadius: 10,
    borderLeftWidth: 4,
    borderLeftColor: '#fc8181',
  },
  disclaimerText: {
    fontSize: 12,
    color: '#c53030',
    textAlign: 'center',
  },
});
