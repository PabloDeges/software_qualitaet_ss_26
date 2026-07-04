<template>
    <div>
      <h1>Wago Lichter</h1>
  
      <div v-if="status" class="lights-grid">
        <div
          v-for="(on, index) in status.lights"
          :key="index"
          class="light-bulb"
          :class="{ on: on }"
        >
          <div class="bulb-label">L{{ index + 1 }}</div>
        </div>
      </div>
      <p v-else class="no-data">Keine Daten vorhanden</p>
  
      <div class="control-section">
        <h2>CTRL cmd senden</h2>
        <div class="control-buttons">
          <button
            v-for="cmd in [0, 1, 2, 3]"
            :key="cmd"
            class="control-btn"
            :class="{ active: lastCommand === cmd }"
            @click="sendCommand(cmd)"
          >
            {{ cmd === 0 ? 'Idle' : `Mode ${cmd}` }}
          </button>
        </div>
        <p v-if="commandStatus" class="command-status">{{ commandStatus }}</p>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, onUnmounted } from 'vue'
  import axios from 'axios'
  
  const API = 'http://localhost:8081'
  const status = ref(null)
  const lastCommand = ref(null)
  const commandStatus = ref('')
  let interval = null
  
  async function fetchStatus() {
    try {
      const res = await axios.get(`${API}/wago/status/latest`)
      status.value = res.data
    } catch (e) {
      console.error('Failed to fetch Wago status', e)
    }
  }
  
  async function sendCommand(cmd) {
    try {
      await axios.post(`${API}/wago/control`, { command: cmd })
      lastCommand.value = cmd
      commandStatus.value = `Command ${cmd} sent successfully`
      setTimeout(() => commandStatus.value = '', 3000)
    } catch (e) {
      commandStatus.value = 'Failed to send command'
    }
  }
  
  onMounted(() => {
    fetchStatus()
    interval = setInterval(fetchStatus, 100)
  })
  
  onUnmounted(() => clearInterval(interval))
  </script>
  
  <style scoped>
  .lights-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    margin-bottom: 32px;
  }
  
  .light-bulb {
    width: 80px;
    text-align: center;
    padding: 12px 8px;
    border-radius: 10px;
    background: #f0f2f5;
    border: 2px solid #ddd;
    transition: all 0.3s;
  }
  
  .light-bulb.on {
    background: #00ff0d;
    border-color: #008e1f;

  }
  
  .bulb-icon { font-size: 28px; }
  .bulb-label { font-size: 12px; margin-top: 4px; color: #666; }
  
  .control-section { margin-top: 8px; }
  
  .control-buttons {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
  }
  
  .control-btn {
    padding: 10px 24px;
    border: none;
    border-radius: 8px;
    background: #4f46e5;
    color: white;
    font-size: 15px;
    cursor: pointer;
    transition: background 0.2s;
  }
  
  .control-btn:hover { background: #4338ca; }
  .control-btn.active { background: #16a34a; }
  
  .command-status {
    margin-top: 10px;
    font-size: 14px;
    color: #16a34a;
  }
  
  .no-data { color: #999; }
  </style>