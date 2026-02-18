import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, TextInput, Alert } from 'react-native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../../App';

type TrustWizardScreenProps = {
  navigation: NativeStackNavigationProp<RootStackParamList, 'TrustWizard'>;
};

interface TrustFormData {
  trustName: string;
  grantorName: string;
  grantorAddress: string;
  beneficiaries: string;
  successorTrustee: string;
  trustType: string;
  assets: string;
  notes: string;
}

export default function TrustWizardScreen({ navigation }: TrustWizardScreenProps) {
  const [step, setStep] = useState(1);
  const [formData, setFormData] = useState<TrustFormData>({
    trustName: '',
    grantorName: '',
    grantorAddress: '',
    beneficiaries: '',
    successorTrustee: '',
    trustType: 'revocable',
    assets: '',
    notes: '',
  });

  const totalSteps = 5;

  const handleNext = () => {
    // Validation before proceeding
    if (step === 1 && !formData.trustName) {
      Alert.alert('⚠️ Required Field', 'Please enter a Trust Name to continue.');
      return;
    }
    if (step === 2 && !formData.grantorName) {
      Alert.alert('⚠️ Required Field', 'Please enter the Grantor Name to continue.');
      return;
    }
    if (step === 3 && !formData.beneficiaries) {
      Alert.alert('⚠️ Required Field', 'Please add at least one Beneficiary to continue.');
      return;
    }
    
    if (step < totalSteps) {
      setStep(step + 1);
    } else {
      // Show confirmation before creating
      Alert.alert(
        '📝 Create Your Living Trust?',
        `Are you sure you want to create "${formData.trustName}"?\n\nThis will generate your Living Trust document.`,
        [
          { text: 'Cancel', style: 'cancel' },
          { 
            text: '✅ Create Now', 
            onPress: () => {
              // Show success after creation
              Alert.alert(
                '✅ Success!',
                `🎉 Your Living Trust "${formData.trustName}" has been created!\n\n📄 Document saved to Documents.\n🤖 AI will review it shortly.`,
                [
                  {
                    text: 'OK',
                    onPress: () => navigation.navigate('Home'),
                  },
                ]
              );
            }
          },
        ]
      );
    }
  };

  const handleBack = () => {
    if (step > 1) {
      setStep(step - 1);
    }
  };

  const updateField = (field: keyof TrustFormData, value: string) => {
    setFormData({ ...formData, [field]: value });
  };

  const renderStepIndicator = () => (
    <View style={styles.stepIndicator}>
      {[1, 2, 3, 4, 5].map((s) => (
        <View
          key={s}
          style={[
            styles.stepDot,
            s <= step ? styles.stepDotActive : styles.stepDotInactive,
          ]}
        >
          <Text style={s <= step ? styles.stepTextActive : styles.stepTextInactive}>
            {s}
          </Text>
        </View>
      ))}
    </View>
  );

  const renderStep1 = () => (
    <View style={styles.stepContent}>
      <Text style={styles.stepTitle}>📋 Step 1: Basic Information</Text>
      
      <Text style={styles.label}>Trust Name *</Text>
      <TextInput
        style={styles.input}
        placeholder="e.g., Smith Family Living Trust"
        placeholderTextColor="#a0aec0"
        value={formData.trustName}
        onChangeText={(text) => updateField('trustName', text)}
      />

      <Text style={styles.label}>Trust Type *</Text>
      <View style={styles.radioGroup}>
        <TouchableOpacity
          style={[
            styles.radioButton,
            formData.trustType === 'revocable' && styles.radioButtonActive,
          ]}
          onPress={() => updateField('trustType', 'revocable')}
        >
          <Text style={styles.radioText}>🔄 Revocable Trust</Text>
          <Text style={styles.radioDesc}>Can be changed during your lifetime</Text>
        </TouchableOpacity>
        
        <TouchableOpacity
          style={[
            styles.radioButton,
            formData.trustType === 'irrevocable' && styles.radioButtonActive,
          ]}
          onPress={() => updateField('trustType', 'irrevocable')}
        >
          <Text style={styles.radioText}>🔒 Irrevocable Trust</Text>
          <Text style={styles.radioDesc}>Cannot be easily changed once created</Text>
        </TouchableOpacity>
      </View>
    </View>
  );

  const renderStep2 = () => (
    <View style={styles.stepContent}>
      <Text style={styles.stepTitle}>👤 Step 2: Grantor Information</Text>
      
      <Text style={styles.label}>Your Full Legal Name *</Text>
      <TextInput
        style={styles.input}
        placeholder="John Doe"
        placeholderTextColor="#a0aec0"
        value={formData.grantorName}
        onChangeText={(text) => updateField('grantorName', text)}
      />

      <Text style={styles.label}>Your Address *</Text>
      <TextInput
        style={[styles.input, styles.textArea]}
        placeholder="123 Main Street, City, State, ZIP"
        placeholderTextColor="#a0aec0"
        value={formData.grantorAddress}
        onChangeText={(text) => updateField('grantorAddress', text)}
        multiline
      />
    </View>
  );

  const renderStep3 = () => (
    <View style={styles.stepContent}>
      <Text style={styles.stepTitle}>👨‍👩‍👧‍👦 Step 3: Beneficiaries</Text>
      
      <Text style={styles.label}>Who will inherit from this Trust? *</Text>
      <TextInput
        style={[styles.input, styles.textArea]}
        placeholder="List beneficiaries with their relationship&#10;e.g.,&#10;- John Smith (Spouse)&#10;- Jane Smith (Daughter)&#10;- Michael Smith (Son)"
        placeholderTextColor="#a0aec0"
        value={formData.beneficiaries}
        onChangeText={(text) => updateField('beneficiaries', text)}
        multiline
        numberOfLines={6}
      />
    </View>
  );

  const renderStep4 = () => (
    <View style={styles.stepContent}>
      <Text style={styles.stepTitle}>👮 Step 4: Successor Trustee</Text>
      
      <Text style={styles.label}>Who will manage the Trust after you? *</Text>
      <TextInput
        style={styles.input}
        placeholder="Name and relationship"
        placeholderTextColor="#a0aec0"
        value={formData.successorTrustee}
        onChangeText={(text) => updateField('successorTrustee', text)}
      />

      <View style={styles.infoBox}>
        <Text style={styles.infoTitle}>ℹ️ What is a Successor Trustee?</Text>
        <Text style={styles.infoText}>
          A successor trustee takes over managing the trust if you become incapacitated or after your passing. 
          This should be someone you trust deeply, like a spouse, adult child, or close family member.
        </Text>
      </View>
    </View>
  );

  const renderStep5 = () => (
    <View style={styles.stepContent}>
      <Text style={styles.stepTitle}>💰 Step 5: Assets & Review</Text>
      
      <Text style={styles.label}>Initial Assets (Optional)</Text>
      <TextInput
        style={[styles.input, styles.textArea]}
        placeholder="List any initial assets to include in the trust"
        placeholderTextColor="#a0aec0"
        value={formData.assets}
        onChangeText={(text) => updateField('assets', text)}
        multiline
        numberOfLines={4}
      />

      <Text style={styles.label}>Additional Notes</Text>
      <TextInput
        style={[styles.input, styles.textArea]}
        placeholder="Any special instructions or requests"
        placeholderTextColor="#a0aec0"
        value={formData.notes}
        onChangeText={(text) => updateField('notes', text)}
        multiline
        numberOfLines={3}
      />

      <View style={styles.summaryBox}>
        <Text style={styles.summaryTitle}>📝 Summary</Text>
        <Text style={styles.summaryText}>Trust: {formData.trustName || 'Not set'}</Text>
        <Text style={styles.summaryText}>Type: {formData.trustType}</Text>
        <Text style={styles.summaryText}>Grantor: {formData.grantorName || 'Not set'}</Text>
      </View>
    </View>
  );

  return (
    <ScrollView style={styles.container}>
      {renderStepIndicator()}
      
      {step === 1 && renderStep1()}
      {step === 2 && renderStep2()}
      {step === 3 && renderStep3()}
      {step === 4 && renderStep4()}
      {step === 5 && renderStep5()}

      <View style={styles.buttonContainer}>
        {step > 1 && (
          <TouchableOpacity style={styles.backButton} onPress={handleBack}>
            <Text style={styles.backButtonText}>← Back</Text>
          </TouchableOpacity>
        )}
        
        <TouchableOpacity 
          style={[
            styles.nextButton,
            step === 1 && !formData.trustName && styles.nextButtonDisabled,
          ]} 
          onPress={handleNext}
          disabled={step === 1 && !formData.trustName}
        >
          <Text style={styles.nextButtonText}>
            {step === totalSteps ? '✅ Create Trust' : 'Next →'}
          </Text>
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f7fa',
  },
  stepIndicator: {
    flexDirection: 'row',
    justifyContent: 'center',
    padding: 20,
    backgroundColor: '#fff',
  },
  stepDot: {
    width: 36,
    height: 36,
    borderRadius: 18,
    justifyContent: 'center',
    alignItems: 'center',
    marginHorizontal: 6,
  },
  stepDotActive: {
    backgroundColor: '#1a365d',
  },
  stepDotInactive: {
    backgroundColor: '#e2e8f0',
  },
  stepTextActive: {
    color: '#fff',
    fontWeight: 'bold',
  },
  stepTextInactive: {
    color: '#718096',
    fontWeight: 'bold',
  },
  stepContent: {
    padding: 20,
  },
  stepTitle: {
    fontSize: 22,
    fontWeight: 'bold',
    color: '#1a365d',
    marginBottom: 20,
  },
  label: {
    fontSize: 16,
    fontWeight: '600',
    color: '#2d3748',
    marginBottom: 8,
    marginTop: 12,
  },
  input: {
    backgroundColor: '#fff',
    borderRadius: 10,
    padding: 14,
    fontSize: 16,
    borderWidth: 1,
    borderColor: '#e2e8f0',
    color: '#2d3748',
  },
  textArea: {
    minHeight: 100,
    textAlignVertical: 'top',
  },
  radioGroup: {
    marginTop: 10,
  },
  radioButton: {
    backgroundColor: '#fff',
    borderRadius: 10,
    padding: 16,
    marginBottom: 10,
    borderWidth: 2,
    borderColor: '#e2e8f0',
  },
  radioButtonActive: {
    borderColor: '#1a365d',
    backgroundColor: '#ebf8ff',
  },
  radioText: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#1a365d',
  },
  radioDesc: {
    fontSize: 13,
    color: '#718096',
    marginTop: 4,
  },
  infoBox: {
    backgroundColor: '#ebf8ff',
    borderRadius: 10,
    padding: 16,
    marginTop: 20,
    borderLeftWidth: 4,
    borderLeftColor: '#4299e1',
  },
  infoTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#2b6cb0',
    marginBottom: 8,
  },
  infoText: {
    fontSize: 14,
    color: '#2c5282',
    lineHeight: 20,
  },
  summaryBox: {
    backgroundColor: '#f0fff4',
    borderRadius: 10,
    padding: 16,
    marginTop: 20,
    borderWidth: 1,
    borderColor: '#68d391',
  },
  summaryTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#276749',
    marginBottom: 10,
  },
  summaryText: {
    fontSize: 14,
    color: '#2f855a',
    marginBottom: 4,
  },
  buttonContainer: {
    flexDirection: 'row',
    padding: 20,
    paddingBottom: 40,
  },
  backButton: {
    flex: 1,
    backgroundColor: '#e2e8f0',
    padding: 16,
    borderRadius: 10,
    marginRight: 10,
    alignItems: 'center',
  },
  backButtonText: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#4a5568',
  },
  nextButton: {
    flex: 2,
    backgroundColor: '#1a365d',
    padding: 16,
    borderRadius: 10,
    alignItems: 'center',
  },
  nextButtonDisabled: {
    backgroundColor: '#a0aec0',
  },
  nextButtonText: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#fff',
  },
});
