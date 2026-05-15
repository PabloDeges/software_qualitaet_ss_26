<template>
  <div class="app">
    <nav>
      <RouterLink to="/wago">Wago 750</RouterLink>
      <RouterLink to="/temperature">Siemens S7-1500</RouterLink>
      <RouterLink to="/gps">GPS</RouterLink>
    </nav>
    <main>
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { RouterLink, RouterView } from 'vue-router'
</script>

<style>
* { box-sizing: border-box; margin: 0; padding: 0; }

body {
  font-family: sans-serif;
  background: #f0f2f5;
  color: #333;
}

.app {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}

nav {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

nav a {
  padding: 10px 20px;
  background: white;
  border-radius: 8px;
  text-decoration: none;
  color: #555;
  font-weight: 500;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

nav a.router-link-active {
  background: #4f46e5;
  color: white;
}

main {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

h1 { font-size: 22px; margin-bottom: 20px; }
h2 { font-size: 16px; margin-bottom: 12px; color: #555; }
</style>