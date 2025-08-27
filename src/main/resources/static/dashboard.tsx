"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Progress } from "@/components/ui/progress"
import { Activity, Users, MapPin, Heart, Zap, AlertTriangle } from "lucide-react"

interface Player {
  id: string
  name: string
  position: string
  jerseyNumber: number
  active: boolean
}

interface PlayerMetrics {
  playerId: string
  movement: {
    totalDistance: number
    sprintDistance: number
    maxSpeed: number
    averageSpeed: number
    sprintCount: number
  }
  performance: {
    maxHeartRate: number
    averageHeartRate: number
    intensityScore: number
    workRate: number
  }
  load: {
    acuteChronicRatio: number
    readinessScore: number
  }
}

export default function Dashboard() {
  const [players, setPlayers] = useState<Player[]>([])
  const [selectedPlayer, setSelectedPlayer] = useState<string>("")
  const [metrics, setMetrics] = useState<PlayerMetrics | null>(null)
  const [isLive, setIsLive] = useState(false)

  useEffect(() => {
    fetchPlayers()
  }, [])

  useEffect(() => {
    if (selectedPlayer) {
      fetchPlayerMetrics(selectedPlayer)
    }
  }, [selectedPlayer])

  const fetchPlayers = async () => {
    try {
      const response = await fetch("/api/players/active")
      const data = await response.json()
      setPlayers(data)
      if (data.length > 0) {
        setSelectedPlayer(data[0].id)
      }
    } catch (error) {
      console.error("Error fetching players:", error)
    }
  }

  const fetchPlayerMetrics = async (playerId: string) => {
    try {
      const response = await fetch(`/api/metrics/player/${playerId}`)
      const data = await response.json()
      if (data.length > 0) {
        setMetrics(data[data.length - 1]) // Get latest metrics
      }
    } catch (error) {
      console.error("Error fetching metrics:", error)
    }
  }

  const formatDistance = (meters: number) => {
    if (meters >= 1000) {
      return `${(meters / 1000).toFixed(2)} km`
    }
    return `${meters.toFixed(0)} m`
  }

  const getInjuryRiskColor = (ratio: number) => {
    if (ratio > 1.5) return "text-red-500"
    if (ratio > 1.2) return "text-yellow-500"
    return "text-green-500"
  }

  const getReadinessColor = (score: number) => {
    if (score >= 8) return "text-green-500"
    if (score >= 6) return "text-yellow-500"
    return "text-red-500"
  }

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Football GPS Dashboard</h1>
            <p className="text-gray-600">Real-time player tracking and analytics</p>
          </div>
          <div className="flex items-center gap-4">
            <Badge variant={isLive ? "default" : "secondary"} className="px-3 py-1">
              <div className={`w-2 h-2 rounded-full mr-2 ${isLive ? "bg-green-500" : "bg-gray-400"}`} />
              {isLive ? "LIVE" : "OFFLINE"}
            </Badge>
            <Button onClick={() => setIsLive(!isLive)} variant={isLive ? "destructive" : "default"}>
              {isLive ? "Stop Session" : "Start Session"}
            </Button>
          </div>
        </div>

        {/* Player Selection */}
        <Card className="mb-6">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Users className="w-5 h-5" />
              Active Players
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-2">
              {players.map((player) => (
                <Button
                  key={player.id}
                  variant={selectedPlayer === player.id ? "default" : "outline"}
                  onClick={() => setSelectedPlayer(player.id)}
                  className="flex items-center gap-2"
                >
                  <span className="font-mono text-sm">#{player.jerseyNumber}</span>
                  {player.name}
                  <Badge variant="secondary" className="text-xs">
                    {player.position}
                  </Badge>
                </Button>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* Main Dashboard */}
        {metrics && (
          <Tabs defaultValue="overview" className="space-y-6">
            <TabsList className="grid w-full grid-cols-4">
              <TabsTrigger value="overview">Overview</TabsTrigger>
              <TabsTrigger value="movement">Movement</TabsTrigger>
              <TabsTrigger value="performance">Performance</TabsTrigger>
              <TabsTrigger value="load">Load Management</TabsTrigger>
            </TabsList>

            <TabsContent value="overview" className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <Card>
                  <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">Total Distance</CardTitle>
                    <MapPin className="h-4 w-4 text-muted-foreground" />
                  </CardHeader>
                  <CardContent>
                    <div className="text-2xl font-bold">{formatDistance(metrics.movement.totalDistance)}</div>
                    <p className="text-xs text-muted-foreground">
                      Sprint: {formatDistance(metrics.movement.sprintDistance)}
                    </p>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">Max Speed</CardTitle>
                    <Zap className="h-4 w-4 text-muted-foreground" />
                  </CardHeader>
                  <CardContent>
                    <div className="text-2xl font-bold">{metrics.movement.maxSpeed.toFixed(1)} km/h</div>
                    <p className="text-xs text-muted-foreground">
                      Avg: {metrics.movement.averageSpeed.toFixed(1)} km/h
                    </p>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">Heart Rate</CardTitle>
                    <Heart className="h-4 w-4 text-muted-foreground" />
                  </CardHeader>
                  <CardContent>
                    <div className="text-2xl font-bold">{metrics.performance.maxHeartRate} bpm</div>
                    <p className="text-xs text-muted-foreground">Avg: {metrics.performance.averageHeartRate} bpm</p>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">Readiness</CardTitle>
                    <Activity className="h-4 w-4 text-muted-foreground" />
                  </CardHeader>
                  <CardContent>
                    <div className={`text-2xl font-bold ${getReadinessColor(metrics.load.readinessScore)}`}>
                      {metrics.load.readinessScore.toFixed(1)}/10
                    </div>
                    <p className="text-xs text-muted-foreground">Player readiness score</p>
                  </CardContent>
                </Card>
              </div>

              {/* Injury Risk Alert */}
              {metrics.load.acuteChronicRatio > 1.3 && (
                <Card className="border-yellow-200 bg-yellow-50">
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2 text-yellow-800">
                      <AlertTriangle className="w-5 h-5" />
                      Injury Risk Alert
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-yellow-700">
                      Acute:Chronic workload ratio is {metrics.load.acuteChronicRatio.toFixed(2)}, indicating elevated
                      injury risk. Consider load management.
                    </p>
                  </CardContent>
                </Card>
              )}
            </TabsContent>

            <TabsContent value="movement" className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <Card>
                  <CardHeader>
                    <CardTitle>Movement Analysis</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="flex justify-between items-center">
                      <span>Total Distance</span>
                      <span className="font-bold">{formatDistance(metrics.movement.totalDistance)}</span>
                    </div>
                    <div className="flex justify-between items-center">
                      <span>Sprint Distance</span>
                      <span className="font-bold">{formatDistance(metrics.movement.sprintDistance)}</span>
                    </div>
                    <div className="flex justify-between items-center">
                      <span>Sprint Count</span>
                      <span className="font-bold">{metrics.movement.sprintCount}</span>
                    </div>
                    <div className="flex justify-between items-center">
                      <span>Max Speed</span>
                      <span className="font-bold">{metrics.movement.maxSpeed.toFixed(1)} km/h</span>
                    </div>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle>Speed Zones</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="space-y-2">
                      <div className="flex justify-between text-sm">
                        <span>Walking (0-7 km/h)</span>
                        <span>25%</span>
                      </div>
                      <Progress value={25} className="h-2" />
                    </div>
                    <div className="space-y-2">
                      <div className="flex justify-between text-sm">
                        <span>Jogging (7-14 km/h)</span>
                        <span>35%</span>
                      </div>
                      <Progress value={35} className="h-2" />
                    </div>
                    <div className="space-y-2">
                      <div className="flex justify-between text-sm">
                        <span>Running (14-19.8 km/h)</span>
                        <span>25%</span>
                      </div>
                      <Progress value={25} className="h-2" />
                    </div>
                    <div className="space-y-2">
                      <div className="flex justify-between text-sm">
                        <span>High Intensity (19.8-24 km/h)</span>
                        <span>10%</span>
                      </div>
                      <Progress value={10} className="h-2" />
                    </div>
                    <div className="space-y-2">
                      <div className="flex justify-between text-sm">
                        <span>Sprinting &gt;24 km/h</span>
                        <span>5%</span>
                      </div>
                      <Progress value={5} className="h-2" />
                    </div>
                  </CardContent>
                </Card>
              </div>
            </TabsContent>

            <TabsContent value="performance" className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <Card>
                  <CardHeader>
                    <CardTitle>Performance Metrics</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="flex justify-between items-center">
                      <span>Work Rate</span>
                      <span className="font-bold">{metrics.performance.workRate.toFixed(1)}%</span>
                    </div>
                    <Progress value={metrics.performance.workRate} className="h-2" />

                    <div className="flex justify-between items-center">
                      <span>Intensity Score</span>
                      <span className="font-bold">{metrics.performance.intensityScore.toFixed(1)}/10</span>
                    </div>
                    <Progress value={metrics.performance.intensityScore * 10} className="h-2" />
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle>Heart Rate Analysis</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="flex justify-between items-center">
                      <span>Max HR</span>
                      <span className="font-bold">{metrics.performance.maxHeartRate} bpm</span>
                    </div>
                    <div className="flex justify-between items-center">
                      <span>Average HR</span>
                      <span className="font-bold">{metrics.performance.averageHeartRate} bpm</span>
                    </div>
                    <div className="flex justify-between items-center">
                      <span>HR Zones</span>
                      <span className="text-sm text-muted-foreground">Zone 4-5: 65%</span>
                    </div>
                  </CardContent>
                </Card>
              </div>
            </TabsContent>

            <TabsContent value="load" className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <Card>
                  <CardHeader>
                    <CardTitle>Load Management</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="flex justify-between items-center">
                      <span>Acute:Chronic Ratio</span>
                      <span className={`font-bold ${getInjuryRiskColor(metrics.load.acuteChronicRatio)}`}>
                        {metrics.load.acuteChronicRatio.toFixed(2)}
                      </span>
                    </div>
                    <div className="text-sm text-muted-foreground">Optimal range: 0.8 - 1.3</div>

                    <div className="flex justify-between items-center">
                      <span>Readiness Score</span>
                      <span className={`font-bold ${getReadinessColor(metrics.load.readinessScore)}`}>
                        {metrics.load.readinessScore.toFixed(1)}/10
                      </span>
                    </div>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle>Recovery Recommendations</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-3">
                      {metrics.load.acuteChronicRatio > 1.3 ? (
                        <div className="p-3 bg-red-50 border border-red-200 rounded-lg">
                          <p className="text-red-800 font-medium">High Risk</p>
                          <p className="text-red-600 text-sm">Reduce training load by 20-30%</p>
                        </div>
                      ) : metrics.load.acuteChronicRatio > 1.2 ? (
                        <div className="p-3 bg-yellow-50 border border-yellow-200 rounded-lg">
                          <p className="text-yellow-800 font-medium">Moderate Risk</p>
                          <p className="text-yellow-600 text-sm">Monitor closely, consider light training</p>
                        </div>
                      ) : (
                        <div className="p-3 bg-green-50 border border-green-200 rounded-lg">
                          <p className="text-green-800 font-medium">Low Risk</p>
                          <p className="text-green-600 text-sm">Training load is well managed</p>
                        </div>
                      )}

                      {metrics.load.readinessScore < 6 && (
                        <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg">
                          <p className="text-blue-800 font-medium">Recovery Focus</p>
                          <p className="text-blue-600 text-sm">Prioritize sleep, nutrition, and active recovery</p>
                        </div>
                      )}
                    </div>
                  </CardContent>
                </Card>
              </div>
            </TabsContent>
          </Tabs>
        )}
      </div>
    </div>
  )
}
