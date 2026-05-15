<template>
    <div>
      <h1>Siemens S7-1500 — Temperature</h1>
  
      <div class="temp-cards">
        <div class="temp-card">
          <div class="temp-label">Actual (Ist)</div>
          <div class="temp-value">
            {{ ist ? ist.value.toFixed(2) + ' °C' : '—' }}
          </div>
        </div>
        <div class="temp-card">
          <div class="temp-label">Target (Soll)</div>
          <div class="temp-value">
            {{ soll ? soll.value.toFixed(2) + ' °C' : '—' }}
          </div>
        </div>
        <div class="temp-card" :class="{ warning: isDiffHigh }">
          <div class="temp-label">Difference (Differenz)</div>
          <div class="temp-value">
            {{ differenz ? differenz.value.toFixed(2) + ' °C' : '—' }}
          </div>
        </div>
      </div>
  
      <p class="hint">Data refreshes every 2 seconds. Waiting for S7-1500 to come online.</p>
    </div>
  </template>
  
  <script setup>
  import { ref, computed, onMounted, onUnmounted } from 'vue'
  import axios from 'axios'
  
  const API = 'http://localhost:8081'
  const ist = ref(null)
  const soll = ref(null)
  const differenz = ref(null)
  let interval = null
  
  const isDiffHigh = computed(() =>
    differenz.value && Math.abs(differenz.value.value) > 5
  )
  
  async function fetchAll() {
    try {
      const [r1, r2, r3] = await Promise.all([
        axios.get(`${API}/temperature/Ist/latest`),
        axios.get(`${API}/temperature/Soll/latest`),
        axios.get(`${API}/temperature/Differenz/latest`)
      ])
      ist.value = r1.data
      soll.value = r2.data
      differenz.value = r3.data
    } catch (e) {
      console.error('Failed to fetch temperatures', e)
    }
  }
  
  onMounted(() => {
    fetchAll()
    interval = setInterval(fetchAll, 2000)
  })
  
  onUnmounted(() => clearInterval(interval))
  </script>
  
  <style scoped>
  .temp-cards {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
    margin-bottom: 24px;
  }
  
  .temp-card {
    flex: 1;
    min-width: 160px;
    padding: 24px;
    border-radius: 12px;
    background: #f0f2f5;
    text-align: center;
    border: 2px solid #e5e7eb;
  }
  
  .temp-card.warning {
    background: #fef2f2;
    border-color: #fca5a5;
  }
  
  .temp-label {
    font-size: 13px;
    color: #666;
    margin-bottom: 10px;
  }
  
  .temp-value {
    font-size: 32px;
    font-weight: 600;
    color: #4f46e5;
  }
  
  .hint {
    font-size: 13px;
    color: #aaa;
  }
  </style>