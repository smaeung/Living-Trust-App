import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { StatusBar } from 'expo-status-bar';
import { SafeAreaProvider } from 'react-native-safe-area-context';

// Screens
import HomeScreen from './src/screens/HomeScreen';
import TrustWizardScreen from './src/screens/TrustWizardScreen';
import ReviewScreen from './src/screens/ReviewScreen';
import AiAssistantScreen from './src/screens/AiAssistantScreen';
import DocumentsScreen from './src/screens/DocumentsScreen';
import SettingsScreen from './src/screens/SettingsScreen';

export type RootStackParamList = {
  Home: undefined;
  TrustWizard: undefined;
  Review: { documentId?: string };
  AiAssistant: undefined;
  Documents: undefined;
  Settings: undefined;
};

const Stack = createNativeStackNavigator<RootStackParamList>();

export default function App() {
  return (
    <SafeAreaProvider>
      <NavigationContainer>
        <Stack.Navigator
          initialRouteName="Home"
          screenOptions={{
            headerStyle: {
              backgroundColor: '#1a365d',
            },
            headerTintColor: '#fff',
            headerTitleStyle: {
              fontWeight: 'bold',
            },
          }}
        >
          <Stack.Screen 
            name="Home" 
            component={HomeScreen}
            options={{ title: 'Living Trust App' }}
          />
          <Stack.Screen 
            name="TrustWizard" 
            component={TrustWizardScreen}
            options={{ title: 'Create Trust' }}
          />
          <Stack.Screen 
            name="Review" 
            component={ReviewScreen}
            options={{ title: 'Review Document' }}
          />
          <Stack.Screen 
            name="AiAssistant" 
            component={AiAssistantScreen}
            options={{ title: 'AI Lawyer Assistant' }}
          />
          <Stack.Screen 
            name="Documents" 
            component={DocumentsScreen}
            options={{ title: 'My Documents' }}
          />
          <Stack.Screen 
            name="Settings" 
            component={SettingsScreen}
            options={{ title: 'Settings' }}
          />
        </Stack.Navigator>
      </NavigationContainer>
      <StatusBar style="light" />
    </SafeAreaProvider>
  );
}
