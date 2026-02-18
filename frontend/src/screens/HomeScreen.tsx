import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, Image } from 'react-native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../App';

type HomeScreenProps = {
  navigation: NativeStackNavigationProp<RootStackParamList, 'Home'>;
};

export default function HomeScreen({ navigation }: HomeScreenProps) {
  return (
    <ScrollView style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.logo}>🛡️</Text>
        <Text style={styles.title}>Living Trust App</Text>
        <Text style={styles.subtitle}>AI-Powered Estate Planning</Text>
      </View>

      {/* Welcome Card */}
      <View style={styles.welcomeCard}>
        <Text style={styles.welcomeTitle}>Welcome Back!</Text>
        <Text style={styles.welcomeText}>
          Your AI lawyer assistant is ready to help you create, review, and manage your Living Trust.
        </Text>
      </View>

      {/* Quick Actions */}
      <Text style={styles.sectionTitle}>Quick Actions</Text>
      
      <TouchableOpacity 
        style={styles.actionCard}
        onPress={() => navigation.navigate('TrustWizard')}
      >
        <Text style={styles.actionIcon}>📝</Text>
        <View style={styles.actionContent}>
          <Text style={styles.actionTitle}>Create New Trust</Text>
          <Text style={styles.actionDesc}>Start a guided wizard to create your Living Trust</Text>
        </View>
        <Text style={styles.arrow}>→</Text>
      </TouchableOpacity>

      <TouchableOpacity 
        style={styles.actionCard}
        onPress={() => navigation.navigate('Review')}
      >
        <Text style={styles.actionIcon}>🔍</Text>
        <View style={styles.actionContent}>
          <Text style={styles.actionTitle}>Review Document</Text>
          <Text style={styles.actionDesc}>Upload and analyze your existing Trust</Text>
        </View>
        <Text style={styles.arrow}>→</Text>
      </TouchableOpacity>

      <TouchableOpacity 
        style={styles.actionCard}
        onPress={() => navigation.navigate('AiAssistant')}
      >
        <Text style={styles.actionIcon}>⚖️</Text>
        <View style={styles.actionContent}>
          <Text style={styles.actionTitle}>AI Lawyer Assistant</Text>
          <Text style={styles.actionDesc}>Get answers to your legal questions</Text>
        </View>
        <Text style={styles.arrow}>→</Text>
      </TouchableOpacity>

      <TouchableOpacity 
        style={styles.actionCard}
        onPress={() => navigation.navigate('Documents')}
      >
        <Text style={styles.actionIcon}>📁</Text>
        <View style={styles.actionContent}>
          <Text style={styles.actionTitle}>My Documents</Text>
          <Text style={styles.actionDesc}>View and manage your Trust documents</Text>
        </View>
        <Text style={styles.arrow}>→</Text>
      </TouchableOpacity>

      {/* Tips Section */}
      <View style={styles.tipsSection}>
        <Text style={styles.tipsTitle}>💡 Tips</Text>
        <Text style={styles.tipText}>
          A Living Trust helps avoid probate, reduce taxes, and protect your assets. 
          Our AI assistant can help you every step of the way!
        </Text>
      </View>

      {/* Footer */}
      <TouchableOpacity 
        style={styles.settingsButton}
        onPress={() => navigation.navigate('Settings')}
      >
        <Text style={styles.settingsText}>⚙️ Settings</Text>
      </TouchableOpacity>
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
    padding: 30,
    alignItems: 'center',
  },
  logo: {
    fontSize: 50,
    marginBottom: 10,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#fff',
  },
  subtitle: {
    fontSize: 16,
    color: '#a0aec0',
    marginTop: 5,
  },
  welcomeCard: {
    backgroundColor: '#fff',
    margin: 15,
    padding: 20,
    borderRadius: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  welcomeTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#1a365d',
    marginBottom: 8,
  },
  welcomeText: {
    fontSize: 15,
    color: '#4a5568',
    lineHeight: 22,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1a365d',
    marginHorizontal: 15,
    marginTop: 10,
    marginBottom: 12,
  },
  actionCard: {
    backgroundColor: '#fff',
    marginHorizontal: 15,
    marginBottom: 12,
    padding: 16,
    borderRadius: 12,
    flexDirection: 'row',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  actionIcon: {
    fontSize: 32,
    marginRight: 15,
  },
  actionContent: {
    flex: 1,
  },
  actionTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#1a365d',
  },
  actionDesc: {
    fontSize: 13,
    color: '#718096',
    marginTop: 3,
  },
  arrow: {
    fontSize: 20,
    color: '#1a365d',
    fontWeight: 'bold',
  },
  tipsSection: {
    backgroundColor: '#ebf8ff',
    margin: 15,
    padding: 16,
    borderRadius: 12,
    borderLeftWidth: 4,
    borderLeftColor: '#4299e1',
  },
  tipsTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#2b6cb0',
    marginBottom: 8,
  },
  tipText: {
    fontSize: 14,
    color: '#2c5282',
    lineHeight: 20,
  },
  settingsButton: {
    backgroundColor: '#edf2f7',
    margin: 15,
    padding: 16,
    borderRadius: 12,
    alignItems: 'center',
  },
  settingsText: {
    fontSize: 16,
    color: '#4a5568',
  },
});
