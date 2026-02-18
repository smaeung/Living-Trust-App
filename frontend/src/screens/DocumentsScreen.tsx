import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, FlatList, Alert, TextInput, Modal } from 'react-native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../../App';

type DocumentsScreenProps = {
  navigation: NativeStackNavigationProp<RootStackParamList, 'Documents'>;
};

interface Document {
  id: string;
  name: string;
  type: 'trust' | 'amendment' | 'other';
  date: string;
  status: 'draft' | 'review' | 'complete';
  size: string;
}

const sampleDocuments: Document[] = [
  {
    id: '1',
    name: 'Smith Family Living Trust',
    type: 'trust',
    date: '2026-02-15',
    status: 'complete',
    size: '245 KB',
  },
  {
    id: '2',
    name: 'Trust Amendment - 2026',
    type: 'amendment',
    date: '2026-02-10',
    status: 'review',
    size: '128 KB',
  },
];

export default function DocumentsScreen({ navigation }: DocumentsScreenProps) {
  const [documents, setDocuments] = useState(sampleDocuments);
  const [linkModalVisible, setLinkModalVisible] = useState(false);
  const [linkUrl, setLinkUrl] = useState('');

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'complete': return '#48bb78';
      case 'review': return '#ecc94b';
      case 'draft': return '#a0aec0';
      default: return '#a0aec0';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'complete': return '✅ Complete';
      case 'review': return '🔄 In Review';
      case 'draft': return '📝 Draft';
      default: return status;
    }
  };

  const getTypeIcon = (type: string) => {
    switch (type) {
      case 'trust': return '📜';
      case 'amendment': return '✏️';
      default: return '📄';
    }
  };

  const handleDelete = (id: string) => {
    Alert.alert(
      'Delete Document',
      'Are you sure you want to delete this document?',
      [
        { text: 'Cancel', style: 'cancel' },
        { 
          text: 'Delete', 
          style: 'destructive',
          onPress: () => setDocuments(documents.filter(d => d.id !== id))
        },
      ]
    );
  };

  const handleUpload = () => {
    // Simulate file upload
    const newDoc: Document = {
      id: Date.now().toString(),
      name: `Uploaded Document ${Date.now()}`,
      type: 'other',
      date: new Date().toISOString().split('T')[0],
      status: 'draft',
      size: Math.floor(Math.random() * 500 + 100) + ' KB',
    };
    setDocuments([newDoc, ...documents]);
    Alert.alert('✅ Success', 'Document uploaded successfully!');
  };

  const handleLink = (url: string) => {
    if (!url.trim()) {
      Alert.alert('⚠️ Error', 'Please enter a valid URL or ID');
      return;
    }
    const newDoc: Document = {
      id: Date.now().toString(),
      name: `Linked Document`,
      type: 'other',
      date: new Date().toISOString().split('T')[0],
      status: 'draft',
      size: '0 KB',
    };
    setDocuments([newDoc, ...documents]);
    Alert.alert('✅ Success', 'Document linked successfully!');
  };

  const renderDocument = ({ item }: { item: Document }) => (
    <TouchableOpacity style={styles.documentCard}>
      <View style={styles.docIcon}>
        <Text style={styles.docIconText}>{getTypeIcon(item.type)}</Text>
      </View>
      <View style={styles.docInfo}>
        <Text style={styles.docName}>{item.name}</Text>
        <View style={styles.docMeta}>
          <Text style={styles.docDate}>📅 {item.date}</Text>
          <Text style={styles.docSize}>📄 {item.size}</Text>
        </View>
        <View style={[styles.statusBadge, { backgroundColor: getStatusColor(item.status) + '20' }]}>
          <Text style={[styles.statusText, { color: getStatusColor(item.status) }]}>
            {getStatusText(item.status)}
          </Text>
        </View>
      </View>
      <View style={styles.docActions}>
        <TouchableOpacity style={styles.actionButton} onPress={() => Alert.alert('View', 'Opening document...')}>
          <Text style={styles.actionIcon}>👁️</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.actionButton} onPress={() => Alert.alert('Download', 'Downloading...')}>
          <Text style={styles.actionIcon}>⬇️</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.actionButton} onPress={() => handleDelete(item.id)}>
          <Text style={styles.actionIcon}>🗑️</Text>
        </TouchableOpacity>
      </View>
    </TouchableOpacity>
  );

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>📁</Text>
        <Text style={styles.headerText}>My Documents</Text>
        <Text style={styles.headerSubtext}>Manage your Living Trust documents</Text>
      </View>

      {/* Stats */}
      <View style={styles.statsContainer}>
        <View style={styles.statCard}>
          <Text style={styles.statNumber}>{documents.length}</Text>
          <Text style={styles.statLabel}>Total Docs</Text>
        </View>
        <View style={styles.statCard}>
          <Text style={styles.statNumber}>{documents.filter(d => d.status === 'complete').length}</Text>
          <Text style={styles.statLabel}>Complete</Text>
        </View>
        <View style={styles.statCard}>
          <Text style={styles.statNumber}>{documents.filter(d => d.status === 'review').length}</Text>
          <Text style={styles.statLabel}>In Review</Text>
        </View>
      </View>

      {/* Document List */}
      <View style={styles.listSection}>
        <Text style={styles.sectionTitle}>📋 All Documents</Text>
        
        {documents.length === 0 ? (
          <View style={styles.emptyState}>
            <Text style={styles.emptyIcon}>📂</Text>
            <Text style={styles.emptyText}>No documents yet</Text>
            <Text style={styles.emptySubtext}>Create your first Living Trust!</Text>
          </View>
        ) : (
          documents.map(doc => (
            <View key={doc.id}>{renderDocument({ item: doc })}</View>
          ))
        )}
      </View>

      {/* Quick Actions */}
      <View style={styles.quickActions}>
        <TouchableOpacity 
          style={styles.quickActionButton}
          onPress={() => navigation.navigate('TrustWizard')}
        >
          <Text style={styles.quickActionIcon}>➕</Text>
          <Text style={styles.quickActionText}>New Document</Text>
        </TouchableOpacity>
        <TouchableOpacity 
          style={styles.quickActionButton}
          onPress={() => {
            Alert.alert(
              '📤 Upload Document',
              'Choose how to upload:',
              [
                { text: '📁 Browse Files', onPress: () => handleUpload() },
                { text: '📷 Take Photo', onPress: () => Alert.alert('Camera', 'Camera upload coming soon!') },
                { text: 'Cancel', style: 'cancel' }
              ]
            );
          }}
        >
          <Text style={styles.quickActionIcon}>📤</Text>
          <Text style={styles.quickActionText}>Upload</Text>
        </TouchableOpacity>
        <TouchableOpacity 
          style={styles.quickActionButton}
          onPress={() => setLinkModalVisible(true)}
        >
          <Text style={styles.quickActionIcon}>🔗</Text>
          <Text style={styles.quickActionText}>Link</Text>
        </TouchableOpacity>
      </View>

      {/* Link Modal */}
      <Modal
        visible={linkModalVisible}
        transparent={true}
        animationType="slide"
        onRequestClose={() => setLinkModalVisible(false)}
      >
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>🔗 Link External Document</Text>
            <TextInput
              style={styles.modalInput}
              placeholder="Enter URL or Document ID"
              placeholderTextColor="#a0aec0"
              value={linkUrl}
              onChangeText={setLinkUrl}
            />
            <View style={styles.modalButtons}>
              <TouchableOpacity 
                style={[styles.modalButton, styles.modalButtonCancel]}
                onPress={() => {
                  setLinkUrl('');
                  setLinkModalVisible(false);
                }}
              >
                <Text style={styles.modalButtonText}>Cancel</Text>
              </TouchableOpacity>
              <TouchableOpacity 
                style={[styles.modalButton, styles.modalButtonConfirm]}
                onPress={() => {
                  if (linkUrl.trim()) {
                    handleLink(linkUrl);
                    setLinkUrl('');
                    setLinkModalVisible(false);
                  } else {
                    Alert.alert('⚠️ Error', 'Please enter a valid URL or ID');
                  }
                }}
              >
                <Text style={styles.modalButtonText}>Link</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>

      {/* Storage Info */}
      <View style={styles.storageSection}>
        <Text style={styles.storageTitle}>💾 Storage Used</Text>
        <View style={styles.storageBar}>
          <View style={styles.storageFill} />
        </View>
        <Text style={styles.storageText}>0.5 MB of 100 MB used</Text>
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
    marginBottom: 8,
  },
  headerText: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#fff',
  },
  headerSubtext: {
    fontSize: 14,
    color: '#a0aec0',
    marginTop: 5,
  },
  statsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    padding: 15,
    backgroundColor: '#fff',
    marginTop: -20,
    marginHorizontal: 15,
    borderRadius: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  statCard: {
    alignItems: 'center',
  },
  statNumber: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#1a365d',
  },
  statLabel: {
    fontSize: 12,
    color: '#718096',
    marginTop: 2,
  },
  listSection: {
    padding: 20,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1a365d',
    marginBottom: 15,
  },
  documentCard: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    flexDirection: 'row',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  docIcon: {
    width: 50,
    height: 50,
    borderRadius: 10,
    backgroundColor: '#edf2f7',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  docIconText: {
    fontSize: 24,
  },
  docInfo: {
    flex: 1,
  },
  docName: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#1a365d',
  },
  docMeta: {
    flexDirection: 'row',
    marginTop: 4,
  },
  docDate: {
    fontSize: 12,
    color: '#718096',
    marginRight: 10,
  },
  docSize: {
    fontSize: 12,
    color: '#718096',
  },
  statusBadge: {
    alignSelf: 'flex-start',
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 12,
    marginTop: 6,
  },
  statusText: {
    fontSize: 12,
    fontWeight: '600',
  },
  docActions: {
    flexDirection: 'column',
  },
  actionButton: {
    padding: 6,
  },
  actionIcon: {
    fontSize: 18,
  },
  emptyState: {
    alignItems: 'center',
    padding: 40,
    backgroundColor: '#fff',
    borderRadius: 12,
  },
  emptyIcon: {
    fontSize: 50,
    marginBottom: 15,
  },
  emptyText: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1a365d',
  },
  emptySubtext: {
    fontSize: 14,
    color: '#718096',
    marginTop: 5,
  },
  quickActions: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    padding: 20,
    paddingTop: 0,
  },
  quickActionButton: {
    backgroundColor: '#fff',
    paddingVertical: 15,
    paddingHorizontal: 25,
    borderRadius: 12,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    elevation: 2,
  },
  quickActionIcon: {
    fontSize: 24,
    marginBottom: 5,
  },
  quickActionText: {
    fontSize: 13,
    color: '#4a5568',
    fontWeight: '500',
  },
  storageSection: {
    margin: 20,
    marginTop: 0,
    backgroundColor: '#fff',
    padding: 16,
    borderRadius: 12,
  },
  storageTitle: {
    fontSize: 14,
    fontWeight: '600',
    color: '#4a5568',
    marginBottom: 10,
  },
  storageBar: {
    height: 8,
    backgroundColor: '#e2e8f0',
    borderRadius: 4,
    overflow: 'hidden',
  },
  storageFill: {
    width: '0.5%',
    height: '100%',
    backgroundColor: '#48bb78',
  },
  storageText: {
    fontSize: 12,
    color: '#718096',
    marginTop: 8,
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalContent: {
    backgroundColor: '#fff',
    borderRadius: 16,
    padding: 24,
    width: '85%',
    maxWidth: 400,
  },
  modalTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#1a365d',
    marginBottom: 20,
    textAlign: 'center',
  },
  modalInput: {
    borderWidth: 1,
    borderColor: '#e2e8f0',
    borderRadius: 10,
    padding: 14,
    fontSize: 16,
    color: '#1a365d',
    marginBottom: 20,
  },
  modalButtons: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  modalButton: {
    flex: 1,
    paddingVertical: 14,
    borderRadius: 10,
    alignItems: 'center',
    marginHorizontal: 5,
  },
  modalButtonCancel: {
    backgroundColor: '#e2e8f0',
  },
  modalButtonConfirm: {
    backgroundColor: '#4299e1',
  },
  modalButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
});
