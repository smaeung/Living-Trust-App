import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, Switch, Alert } from 'react-native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../../App';

type SettingsScreenProps = {
  navigation: NativeStackNavigationProp<RootStackParamList, 'Settings'>;
};

export default function SettingsScreen({ navigation }: SettingsScreenProps) {
  const [notifications, setNotifications] = useState(true);
  const [emailUpdates, setEmailUpdates] = useState(false);
  const [darkMode, setDarkMode] = useState(false);
  const [biometricLogin, setBiometricLogin] = useState(false);

  const handleLogout = () => {
    Alert.alert(
      '🚪 Logout',
      'Are you sure you want to logout?',
      [
        { text: 'Cancel', style: 'cancel' },
        { text: 'Logout', style: 'destructive', onPress: () => navigation.navigate('Home') },
      ]
    );
  };

  const handleDeleteAccount = () => {
    Alert.alert(
      '⚠️ Delete Account',
      'This action cannot be undone. All your data will be permanently deleted.',
      [
        { text: 'Cancel', style: 'cancel' },
        { 
          text: 'Delete', 
          style: 'destructive',
          onPress: () => Alert.alert('✅ Done', 'Your account has been deleted.')
        },
      ]
    );
  };

  const handleEditProfile = () => {
    Alert.alert('✏️ Edit Profile', 'Opening profile editor...\n\nFeatures coming soon:\n• Change name\n• Update email\n• Update phone number');
  };

  const handleChangePassword = () => {
    Alert.alert('🔐 Change Password', 'Password change form coming soon!');
  };

  const handle2FA = () => {
    Alert.alert('📱 Two-Factor Authentication', '2FA setup coming soon!\n\nOptions:\n• SMS verification\n• Authenticator app\n• Email code');
  };

  const handleLanguage = () => {
    Alert.alert(
      '🌐 Select Language',
      'Choose your preferred language:',
      [
        { text: 'English ✓', onPress: () => {} },
        { text: 'Spanish', onPress: () => {} },
        { text: 'French', onPress: () => {} },
        { text: 'Cancel', style: 'cancel' }
      ]
    );
  };

  const handleClearCache = () => {
    Alert.alert(
      '💾 Clear Cache',
      'This will remove temporary files. Your data will not be affected.',
      [
        { text: 'Cancel', style: 'cancel' },
        { 
          text: 'Clear', 
          onPress: () => Alert.alert('✅ Done', 'Cache cleared successfully!')
        },
      ]
    );
  };

  const handleTerms = () => {
    Alert.alert('📜 Terms of Service', 'By using this app, you agree to our Terms of Service.\n\n© 2026 Living Trust App');
  };

  const handlePrivacy = () => {
    Alert.alert('🔒 Privacy Policy', 'Your data is protected.\n\n• We never share your information\n• Documents are encrypted\n• Secure authentication');
  };

  const handleDisclaimer = () => {
    Alert.alert('📋 Disclaimer', '⚠️ IMPORTANT\n\nThis app provides general information about Living Trusts. It is NOT legal advice.\n\nPlease consult with a licensed attorney for legal guidance specific to your situation.');
  };

  const handleRateApp = () => {
    Alert.alert('⭐ Rate the App', 'Thank you for your support!\n\nPlease rate us on the app store.');
  };

  const handleFeedback = () => {
    Alert.alert('💬 Send Feedback', 'We\'d love to hear from you!\n\nEmail: support@livingtrustapp.com');
  };

  const handleNotificationToggle = (value: boolean, setting: string) => {
    if (setting === 'notifications') {
      setNotifications(value);
      Alert.alert(value ? '🔔 Enabled' : '🔕 Disabled', `${setting} ${value ? 'enabled' : 'disabled'}`);
    } else if (setting === 'email') {
      setEmailUpdates(value);
      Alert.alert(value ? '📧 Enabled' : '📧 Disabled', `Email updates ${value ? 'enabled' : 'disabled'}`);
    } else if (setting === 'biometric') {
      setBiometricLogin(value);
      Alert.alert(value ? '👆 Enabled' : '👆 Disabled', `Biometric login ${value ? 'enabled' : 'disabled'}`);
    } else if (setting === 'darkMode') {
      setDarkMode(value);
      Alert.alert(value ? '🌙 Dark Mode' : '☀️ Light Mode', `Theme changed to ${value ? 'dark' : 'light'} mode`);
    }
  };

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>⚙️</Text>
        <Text style={styles.headerText}>Settings</Text>
      </View>

      {/* Profile Section */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>👤 Profile</Text>
        
        <TouchableOpacity style={styles.settingItem} onPress={handleEditProfile}>
          <View style={styles.settingContent}>
            <Text style={styles.settingIcon}>✏️</Text>
            <View>
              <Text style={styles.settingLabel}>Edit Profile</Text>
              <Text style={styles.settingValue}>Name, email, phone</Text>
            </View>
          </View>
          <Text style={styles.arrow}>›</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.settingItem} onPress={handleChangePassword}>
          <View style={styles.settingContent}>
            <Text style={styles.settingIcon}>🔐</Text>
            <View>
              <Text style={styles.settingLabel}>Change Password</Text>
              <Text style={styles.settingValue}>Update your password</Text>
            </View>
          </View>
          <Text style={styles.arrow}>›</Text>
        </TouchableOpacity>
      </View>

      {/* Notifications Section */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>🔔 Notifications</Text>
        
        <View style={styles.settingItem}>
          <View style={styles.settingContent}>
            <Text style={styles.settingIcon}>🔔</Text>
            <View>
              <Text style={styles.settingLabel}>Push Notifications</Text>
              <Text style={styles.settingValue}>Get updates on your device</Text>
            </View>
          </View>
          <Switch
            value={notifications}
            onValueChange={(value) => handleNotificationToggle(value, 'notifications')}
            trackColor={{ false: '#e2e8f0', true: '#48bb78' }}
            thumbColor="#fff"
          />
        </View>

        <View style={styles.settingItem}>
          <View style={styles.settingContent}>
            <Text style={styles.settingIcon}>📧</Text>
            <View>
              <Text style={styles.settingLabel}>Email Updates</Text>
              <Text style={styles.settingValue}>Weekly digest and alerts</Text>
            </View>
          </View>
          <Switch
            value={emailUpdates}
            onValueChange={(value) => handleNotificationToggle(value, 'email')}
            trackColor={{ false: '#e2e8f0', true: '#48bb78' }}
            thumbColor="#fff"
          />
        </View>
      </View>

      {/* Security Section */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>🛡️ Security</Text>
        
        <View style={styles.settingItem}>
          <View style={styles.settingContent}>
            <Text style={styles.settingIcon}>👆</Text>
            <View>
              <Text style={styles.settingLabel}>Biometric Login</Text>
              <Text style={styles.settingValue}>Face ID / Fingerprint</Text>
            </View>
          </View>
          <Switch
            value={biometricLogin}
            onValueChange={(value) => handleNotificationToggle(value, 'biometric')}
            trackColor={{ false: '#e2e8f0', true: '#48bb78' }}
            thumbColor="#fff"
          />
        </View>

        <TouchableOpacity style={styles.settingItem} onPress={handle2FA}>
          <View style={styles.settingContent}>
            <Text style={styles.settingIcon}>📱</Text>
            <View>
              <Text style={styles.settingLabel}>Two-Factor Authentication</Text>
              <Text style={styles.settingValue}>Add extra security</Text>
            </View>
          </View>
          <Text style={styles.arrow}>›</Text>
        </TouchableOpacity>
      </View>

      {/* App Settings */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>📱 App</Text>
        
        <View style={styles.settingItem}>
          <View style={styles.settingContent}>
            <Text style={styles.settingIcon}>🌙</Text>
            <View>
              <Text style={styles.settingLabel}>Dark Mode</Text>
              <Text style={styles.settingValue}>Use dark theme</Text>
            </View>
          </View>
          <Switch
            value={darkMode}
            onValueChange={(value) => handleNotificationToggle(value, 'darkMode')}
            trackColor={{ false: '#e2e8f0', true: '#48bb78' }}
            thumbColor="#fff"
          />
        </View>

        <TouchableOpacity style={styles.settingItem} onPress={handleLanguage}>
          <View style={styles.settingContent}>
            <Text style={styles.settingIcon}>🌐</Text>
            <View>
              <Text style={styles.settingLabel}>Language</Text>
              <Text style={styles.settingValue}>English</Text>
            </View>
          </View>
          <Text style={styles.arrow}>›</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.settingItem} onPress={handleClearCache}>
          <View style={styles.settingContent}>
            <Text style={styles.settingIcon}>💾</Text>
            <View>
              <Text style={styles.settingLabel}>Clear Cache</Text>
              <Text style={styles.settingValue}>Free up storage space</Text>
            </View>
          </View>
          <Text style={styles.arrow}>›</Text>
        </TouchableOpacity>
      </View>

      {/* Legal Section */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>📄 Legal</Text>
        
        <TouchableOpacity style={styles.settingItem} onPress={handleTerms}>
          <View style={styles.settingContent}>
            <Text style={styles.settingIcon}>📜</Text>
            <Text style={styles.settingLabel}>Terms of Service</Text>
          </View>
          <Text style={styles.arrow}>›</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.settingItem} onPress={handlePrivacy}>
          <View style={styles.settingContent}>
            <Text style={styles.settingIcon}>🔒</Text>
            <Text style={styles.settingLabel}>Privacy Policy</Text>
          </View>
          <Text style={styles.arrow}>›</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.settingItem} onPress={handleDisclaimer}>
          <View style={styles.settingContent}>
            <Text style={styles.settingIcon}>📋</Text>
            <Text style={styles.settingLabel}>Disclaimer</Text>
          </View>
          <Text style={styles.arrow}>›</Text>
        </TouchableOpacity>
      </View>

      {/* About Section */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>ℹ️ About</Text>
        
        <TouchableOpacity style={styles.settingItem} onPress={handleRateApp}>
          <View style={styles.settingContent}>
            <Text style={styles.settingIcon}>⭐</Text>
            <Text style={styles.settingLabel}>Rate the App</Text>
          </View>
          <Text style={styles.arrow}>›</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.settingItem} onPress={handleFeedback}>
          <View style={styles.settingContent}>
            <Text style={styles.settingIcon}>💬</Text>
            <Text style={styles.settingLabel}>Send Feedback</Text>
          </View>
          <Text style={styles.arrow}>›</Text>
        </TouchableOpacity>

        <View style={styles.settingItem}>
          <View style={styles.settingContent}>
            <Text style={styles.settingIcon}>📱</Text>
            <View>
              <Text style={styles.settingLabel}>Version</Text>
              <Text style={styles.settingValue}>1.0.0</Text>
            </View>
          </View>
        </View>
      </View>

      {/* Account Actions */}
      <View style={styles.section}>
        <TouchableOpacity style={styles.logoutButton} onPress={handleLogout}>
          <Text style={styles.logoutButtonText}>🚪 Logout</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.deleteButton} onPress={handleDeleteAccount}>
          <Text style={styles.deleteButtonText}>🗑️ Delete Account</Text>
        </TouchableOpacity>
      </View>

      <Text style={styles.footer}>© 2026 Living Trust App. All rights reserved.</Text>
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
    marginBottom: 5,
  },
  headerText: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#fff',
  },
  section: {
    backgroundColor: '#fff',
    marginTop: 20,
    marginHorizontal: 15,
    borderRadius: 12,
    padding: 5,
  },
  sectionTitle: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#718096',
    textTransform: 'uppercase',
    padding: 15,
    paddingBottom: 5,
  },
  settingItem: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: 15,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
  },
  settingContent: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  settingIcon: {
    fontSize: 20,
    marginRight: 15,
  },
  settingLabel: {
    fontSize: 16,
    color: '#2d3748',
  },
  settingValue: {
    fontSize: 13,
    color: '#718096',
    marginTop: 2,
  },
  arrow: {
    fontSize: 24,
    color: '#cbd5e0',
  },
  logoutButton: {
    backgroundColor: '#edf2f7',
    margin: 15,
    padding: 16,
    borderRadius: 10,
    alignItems: 'center',
  },
  logoutButtonText: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#4a5568',
  },
  deleteButton: {
    backgroundColor: '#fff5f5',
    marginHorizontal: 15,
    marginBottom: 15,
    padding: 16,
    borderRadius: 10,
    alignItems: 'center',
  },
  deleteButtonText: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#e53e3e',
  },
  footer: {
    textAlign: 'center',
    color: '#a0aec0',
    fontSize: 12,
    padding: 20,
  },
});
