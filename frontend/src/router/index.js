import { createRouter, createWebHistory } from 'vue-router'
import WagoView from '../views/WagoView.vue'
import TemperatureView from '../views/TemperatureView.vue'
import GpsView from '../views/GpsView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/wago' },
    { path: '/wago', component: WagoView },
    { path: '/temperature', component: TemperatureView },
    { path: '/gps', component: GpsView }
  ]
})

export default router