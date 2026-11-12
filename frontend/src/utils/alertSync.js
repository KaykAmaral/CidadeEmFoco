function synchronizeAlerts(alerts, deletedIds = new Set()) {
  return [...new Map(
    (Array.isArray(alerts) ? alerts : [])
      .filter((alert) => !deletedIds.has(alert.id))
      .map((alert) => [alert.id, alert]),
  ).values()]
}

function canDeleteAlert(role) {
  return role === 'ADMIN'
}

function isLatestAlertRequest(requestId, latestRequestId) {
  return requestId === latestRequestId
}

export { canDeleteAlert, isLatestAlertRequest, synchronizeAlerts }
