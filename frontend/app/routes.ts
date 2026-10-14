import { type RouteConfig, index, route } from '@react-router/dev/routes'

export default [
  index('routes/HomeRoute.tsx'),
  route('category/:categoryType', 'routes/CategoryRoute.tsx'),
  route('health', 'routes/HealthRoute.tsx'),
] satisfies RouteConfig
