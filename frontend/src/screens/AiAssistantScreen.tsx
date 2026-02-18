import React, { useState } from 'react';
import { View, Text, StyleSheet, ScrollView, TextInput, TouchableOpacity, FlatList, KeyboardAvoidingView, Platform } from 'react-native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../App';

type AiAssistantScreenProps = {
  navigation: NativeStackNavigationProp<RootStackParamList, 'AiAssistant'>;
};

interface Message {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
}

const quickQuestions = [
  { id: '1', question: 'What is a Living Trust?' },
  { id: '2', question: 'Revocable vs Irrevocable?' },
  { id: '3', question: 'Do I need a lawyer?' },
  { id: '4', question: 'How much does it cost?' },
];

export default function AiAssistantScreen({ navigation }: AiAssistantScreenProps) {
  const [messages, setMessages] = useState<Message[]>([
    {
      id: '1',
      role: 'assistant',
      content: 'Hello! I\'m your AI Lawyer Assistant. I can help you understand Living Trusts, review your documents, and answer legal questions.\n\n**How can I help you today?**',
      timestamp: new Date(),
    },
  ]);
  const [inputText, setInputText] = useState('');
  const [isTyping, setIsTyping] = useState(false);

  const handleSend = () => {
    if (!inputText.trim()) return;

    const userMessage: Message = {
      id: Date.now().toString(),
      role: 'user',
      content: inputText,
      timestamp: new Date(),
    };

    setMessages(prev => [...prev, userMessage]);
    setInputText('');
    setIsTyping(true);

    // Simulate AI response
    setTimeout(() => {
      const aiResponse: Message = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content: getAIResponse(inputText),
        timestamp: new Date(),
      };
      setMessages(prev => [...prev, aiResponse]);
      setIsTyping(false);
    }, 1500);
  };

  const handleQuickQuestion = (question: string) => {
    setInputText(question);
    setTimeout(() => handleSend(), 100);
  };

  const getAIResponse = (question: string): string => {
    const q = question.toLowerCase();
    
    if (q.includes('what is') && q.includes('living trust')) {
      return `A **Living Trust** is a legal document that holds your assets during your lifetime and distributes them to your beneficiaries after you pass away.\n\n**Key benefits:**\n\n- ✅ **Avoids probate** - No court involvement\n- ✅ **Maintains privacy** - Not public record\n- ✅ **Can be revoked** - Change anytime while alive\n\nWould you like more details on how to create one?`;
    }
    
    if (q.includes('revocable') || q.includes('irrevocable')) {
      return `Great question! Here's the difference:\n\n## 🔄 Revocable Living Trust\n- **You keep control** - Can change or cancel anytime\n- **No tax benefits** - Still taxed as personal income\n- **Most people choose this** - More flexibility\n\n## 🔒 Irrevocable Living Trust\n- **Permanent** - Very hard to change once created\n- **Tax benefits** - May reduce estate taxes\n- **Asset protection** - May protect from creditors\n\n**Recommendation:** Most people start with a **revocable trust** for flexibility.`;
    }
    
    if (q.includes('lawyer') || q.includes('attorney')) {
      return `Whether you need a lawyer depends on:\n\n**You might NOT need a lawyer if:**\n- Your estate is simple (< $500K)\n- You use a reliable online service\n- You\'re comfortable with legal forms\n\n**You SHOULD get a lawyer if:**\n- Your estate is complex (>$1M)\n- You have business interests\n- There\'s family conflict\n- You want customized provisions\n\n**Our app can help** with document review and guidance, but complex situations may need professional legal advice.`;
    }
    
    if (q.includes('cost') || q.includes('price') || q.includes('expensive')) {
      return `Here\'s what Living Trust costs typically look like:\n\n| Option | Cost |\n|--------|------|\n| **DIY Online** | $99 - $299 |\n| **Attorney Draft** | $1,000 - $3,000 |\n| **Complex Estate** | $3,000 - $10,000+ |\n\n**Our app helps you:**\n- ✅ Create a basic trust for less\n- ✅ Review existing documents\n- ✅ Understand what you need\n- ✅ Know when to hire an attorney\n\nWould you like help creating your trust?`;
    }

    return `I understand you're asking about "${question}". \n\nAs your AI assistant, I can help with:\n\n- 📝 **Creating a Living Trust** - Guided wizard\n- 🔍 **Reviewing documents** - AI-powered analysis\n- ❓ **Answering questions** - Legal information\n\n**Could you be more specific?** For example, ask "How do I create a trust?" or "What should I include?".`;
  };

  const renderMessage = ({ item }: { item: Message }) => (
    <View style={[
      styles.messageContainer,
      item.role === 'user' ? styles.userMessageContainer : styles.assistantMessageContainer
    ]}>
      <View style={[
        styles.messageBubble,
        item.role === 'user' ? styles.userBubble : styles.assistantBubble
      ]}>
        <Text style={styles.messageText}>{item.content}</Text>
        <Text style={styles.timestamp}>
          {item.timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
        </Text>
      </View>
    </View>
  );

  return (
    <KeyboardAvoidingView 
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
    >
      <View style={styles.header}>
        <Text style={styles.headerTitle}>⚖️</Text>
        <Text style={styles.headerText}>AI Lawyer Assistant</Text>
      </View>

      {/* Quick Questions */}
      <ScrollView horizontal style={styles.quickQuestions} showsHorizontalScrollIndicator={false}>
        {quickQuestions.map((item) => (
          <TouchableOpacity
            key={item.id}
            style={styles.quickQuestionButton}
            onPress={() => handleQuickQuestion(item.question)}
          >
            <Text style={styles.quickQuestionText}>{item.question}</Text>
          </TouchableOpacity>
        ))}
      </ScrollView>

      <FlatList
        data={messages}
        renderItem={renderMessage}
        keyExtractor={item => item.id}
        style={styles.messageList}
        contentContainerStyle={styles.messageListContent}
      />

      {isTyping && (
        <View style={styles.typingIndicator}>
          <Text style={styles.typingText}>🤖 AI is typing...</Text>
        </View>
      )}

      <View style={styles.inputContainer}>
        <TextInput
          style={styles.input}
          placeholder="Ask your legal question..."
          placeholderTextColor="#a0aec0"
          value={inputText}
          onChangeText={setInputText}
          multiline
          maxLength={500}
        />
        <TouchableOpacity 
          style={[styles.sendButton, !inputText.trim() && styles.sendButtonDisabled]}
          onPress={handleSend}
          disabled={!inputText.trim()}
        >
          <Text style={styles.sendButtonText}>➤</Text>
        </TouchableOpacity>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f7fa',
  },
  header: {
    backgroundColor: '#1a365d',
    padding: 20,
    alignItems: 'center',
  },
  headerTitle: {
    fontSize: 30,
    marginBottom: 5,
  },
  headerText: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#fff',
  },
  quickQuestions: {
    backgroundColor: '#fff',
    paddingVertical: 12,
    paddingHorizontal: 10,
    maxHeight: 50,
  },
  quickQuestionButton: {
    backgroundColor: '#ebf8ff',
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 20,
    marginHorizontal: 5,
    borderWidth: 1,
    borderColor: '#4299e1',
  },
  quickQuestionText: {
    fontSize: 13,
    color: '#2b6cb0',
    fontWeight: '500',
  },
  messageList: {
    flex: 1,
  },
  messageListContent: {
    padding: 15,
    paddingBottom: 20,
  },
  messageContainer: {
    marginBottom: 15,
  },
  userMessageContainer: {
    alignItems: 'flex-end',
  },
  assistantMessageContainer: {
    alignItems: 'flex-start',
  },
  messageBubble: {
    maxWidth: '80%',
    padding: 14,
    borderRadius: 16,
  },
  userBubble: {
    backgroundColor: '#1a365d',
    borderBottomRightRadius: 2,
  },
  assistantBubble: {
    backgroundColor: '#fff',
    borderBottomLeftRadius: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    elevation: 2,
  },
  messageText: {
    fontSize: 15,
    color: '#2d3748',
    lineHeight: 22,
  },
  timestamp: {
    fontSize: 11,
    color: '#a0aec0',
    marginTop: 6,
    textAlign: 'right',
  },
  typingIndicator: {
    padding: 10,
    paddingLeft: 20,
  },
  typingText: {
    fontSize: 13,
    color: '#718096',
    fontStyle: 'italic',
  },
  inputContainer: {
    flexDirection: 'row',
    padding: 12,
    backgroundColor: '#fff',
    borderTopWidth: 1,
    borderTopColor: '#e2e8f0',
    alignItems: 'flex-end',
  },
  input: {
    flex: 1,
    backgroundColor: '#f5f7fa',
    borderRadius: 20,
    paddingHorizontal: 16,
    paddingVertical: 10,
    fontSize: 15,
    maxHeight: 100,
    color: '#2d3748',
  },
  sendButton: {
    backgroundColor: '#1a365d',
    width: 44,
    height: 44,
    borderRadius: 22,
    justifyContent: 'center',
    alignItems: 'center',
    marginLeft: 10,
  },
  sendButtonDisabled: {
    backgroundColor: '#a0aec0',
  },
  sendButtonText: {
    fontSize: 18,
    color: '#fff',
  },
});
