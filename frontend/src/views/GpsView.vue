<template>
    <div>
      <h1>GPS Position</h1>
  
      <div v-if="gps" class="gps-cards">
        <div class="gps-card">
          <div class="gps-label">Latitude</div>
          <div class="gps-value">{{ gps.latitude }}</div>
        </div>
        <div class="gps-card">
          <div class="gps-label">Longitude</div>
          <div class="gps-value">{{ gps.longitude }}</div>
        </div>
        <div class="gps-card">
          <div class="gps-label">Altitude</div>
          <div class="gps-value">{{ gps.altitude }} m</div>
        </div>
      </div>
      <p v-else class="no-data">No GPS data available yet.</p>
  
      
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, onUnmounted } from 'vue'
  import axios from 'axios'
  
  const API = 'http://localhost:8081'
  const gps = ref(null)
  let interval = null
  
  async function fetchGps() {
    try {
      const res = await axios.get(`${API}/gps/latest`)
      gps.value = res.data
    } catch (e) {
      console.error('Failed to fetch GPS', e)
    }
  }
  
  onMounted(() => {
    fetchGps()
    interval = setInterval(fetchGps, 5000)
  })
  
  onUnmounted(() => clearInterval(interval))
  </script>
  
  <style scoped>
  .gps-cards {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
    margin-bottom: 24px;
  }
  
  .gps-card {
    flex: 1;
    min-width: 160px;
    padding: 24px;
    border-radius: 12px;
    background: #f0f2f5;
    text-align: center;
    border: 2px solid #e5e7eb;
  }
  
  .gps-label {
    font-size: 13px;
    color: #666;
    margin-bottom: 10px;
  }
  
  .gps-value {
    font-size: 24px;
    font-weight: 600;
    color: #4f46e5;
  }
  
  .map-link a {
    color: #4f46e5;
    font-size: 14px;
  }
  
  .no-data { color: #999; }
  </style>